package com.vocabulary.myvocabulary.repositories.dictionary

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.vocabulary.myvocabulary.DispatcherProvider
import com.vocabulary.myvocabulary.repositories.AppDatabase
import com.vocabulary.myvocabulary.repositories.sync.CloudSyncRepository
import com.vocabulary.myvocabulary.repositories.sync.DeleteDictionaryWorker
import com.vocabulary.myvocabulary.repositories.sync.DownloadDictionariesWorker
import com.vocabulary.myvocabulary.repositories.sync.SyncDictionaryWorker
import com.vocabulary.myvocabulary.repositories.sync.toLocal
import com.vocabulary.myvocabulary.repositories.word.WordDao
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import android.util.Log
import com.vocabulary.myvocabulary.ui.words.toWordEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar

class DictionaryRepositoryImpl(
    private val dictionaryDao: DictionaryDao,
    private val wordDao: WordDao,
    private val cloudSyncRepository: CloudSyncRepository,
    private val workManager: WorkManager,
    private val appDatabase: AppDatabase,
    private val dispatchers: DispatcherProvider
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
        triggerDeleteSync(dictionary.dictionaryId)
    }

    override suspend fun updateDictionary(dictionary: Dictionary) {
        dictionaryDao.updateDictionary(dictionary.toDictionaryEntry())
        triggerSync(dictionary.dictionaryId)
    }

    private fun triggerDeleteSync(dictionaryId: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val deleteRequest = OneTimeWorkRequestBuilder<DeleteDictionaryWorker>()
            .setConstraints(constraints)
            .setInputData(
                Data.Builder()
                    .putLong(DeleteDictionaryWorker.KEY_DICTIONARY_ID, dictionaryId)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "delete_$dictionaryId",
            ExistingWorkPolicy.REPLACE,
            deleteRequest
        )
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
        Log.d("Sync", "syncFromCloud called for user: $userId, requireWifi: $requireWifi")
        if (requireWifi) {
            triggerDownload() // Removed unnecessary 'true'
            return Result.success(Unit)
        }

        return withContext(dispatchers.io) {
            runCatching {
                val cloudData = cloudSyncRepository.downloadDictionaries(userId).getOrThrow()
                Log.d("Sync", "Downloaded ${cloudData.size} dictionaries from cloud for user $userId")

                cloudData.forEach { (cloudDict, cloudWords) ->
                    Log.d("Sync", "Restoring dictionary: ${cloudDict.name} (ID: ${cloudDict.id}) with ${cloudWords.size} words")
                    dictionaryDao.insertDictionary(cloudDict.toLocal().toDictionaryEntry())

                    cloudWords.forEach { cloudWord ->
                        wordDao.insertWord(cloudWord.toLocal().toWordEntry())
                    }
                }
            }.onFailure {
                Log.e("Sync", "Error in syncFromCloud", it)
            }
        }
    }

    private fun triggerDownload() { // Removed unused parameter
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadDictionariesWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "initial_download",
            ExistingWorkPolicy.REPLACE,
            downloadRequest
        )
    }

    override suspend fun clearLocalData() {
        withContext(dispatchers.io) {
            appDatabase.clearAllTables()
        }
    }
}