package com.vocabulary.myvocabulary.repositories.dictionary

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.vocabulary.myvocabulary.repositories.sync.CloudSyncRepository
import com.vocabulary.myvocabulary.repositories.sync.DownloadDictionariesWorker
import com.vocabulary.myvocabulary.repositories.sync.SyncDictionaryWorker
import com.vocabulary.myvocabulary.repositories.sync.toLocal
import com.vocabulary.myvocabulary.repositories.word.WordDao
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import com.vocabulary.myvocabulary.ui.words.toWordEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

class DictionaryRepositoryImpl(
    private val dictionaryDao: DictionaryDao,
    private val wordDao: WordDao,
    private val cloudSyncRepository: CloudSyncRepository,
    private val workManager: WorkManager
) : DictionaryRepository {
    override val allDictionaries: Flow<List<Dictionary>> = dictionaryDao.getAllDictionaries()
        .map { list ->
            list.map { it.toDictionary() }
        }
    override val numberOfDictionaries: Flow<Int> = allDictionaries.map { it.size }

    override suspend fun createDictionary(dictionary: Dictionary): Long {
        val id = dictionaryDao.insertDictionary(dictionary.toDictionaryEntry())
        triggerSync(id)
        return id
    }

    override suspend fun deleteDictionary(dictionary: Dictionary) {
        dictionaryDao.deleteDictionary(dictionary.toDictionaryEntry())
        // For deletion, we would ideally enqueue a Cloud Delete worker.
    }

    override suspend fun updateDictionary(dictionary: Dictionary) {
        dictionaryDao.updateDictionary(dictionary.toDictionaryEntry())
        triggerSync(dictionary.dictionaryId)
    }

    private fun triggerSync(dictionaryId: Long, requireWifi: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (requireWifi) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncDictionaryWorker>()
            .setConstraints(constraints) // The system now manages the "Wait" for us!
            .setInputData(
                Data.Builder()
                    .putLong(SyncDictionaryWorker.KEY_DICTIONARY_ID, dictionaryId)
                    .build()
            )
            .build()

        // Use UNIQUE work so we don't spam the system with the same dictionary
        workManager.enqueueUniqueWork(
            "sync_$dictionaryId",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    override suspend fun getDictionaryById(dictionaryId: Long): Dictionary {
        return allDictionaries
            .map { list ->
                list.first {
                    it.dictionaryId == dictionaryId
                }
            }.first()
    }

    override suspend fun onQuizFinished(dictionaryId: Long?) {
        if (dictionaryId != null) {
            dictionaryDao.updateLastPracticed(dictionaryId, Calendar.getInstance().time)
        }
    }

    override suspend fun saveQuizStats(id: Long, scorePercentage: Int) {
        dictionaryDao.updateDictionaryStats(id, Calendar.getInstance().time, scorePercentage)
    }

    override suspend fun syncAllToCloud(requireWifi: Boolean) {
        val localDictionaries = allDictionaries.first()
        localDictionaries.forEach { dictionary ->
            triggerSync(dictionary.dictionaryId, requireWifi)
        }
    }

    override suspend fun syncFromCloud(userId: String, requireWifi: Boolean): Result<Unit> {
        if (requireWifi) {
            triggerDownload(requireWifi = true)
            return Result.success(Unit)
        }

        return runCatching {
            val cloudData = cloudSyncRepository.downloadDictionaries(userId).getOrThrow()

            cloudData.forEach { (cloudDict, cloudWords) ->
                dictionaryDao.insertDictionary(cloudDict.toLocal().toDictionaryEntry())

                cloudWords.forEach { cloudWord ->
                    wordDao.insertWord(cloudWord.toLocal().toWordEntry())
                }
            }
        }
    }

    private fun triggerDownload(requireWifi: Boolean) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (requireWifi) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .build()

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadDictionariesWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "initial_download",
            ExistingWorkPolicy.KEEP,
            downloadRequest
        )
    }
}