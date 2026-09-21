package com.vocabulary.myvocabulary.repositories.dictionary

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.vocabulary.myvocabulary.repositories.sync.SyncDictionaryWorker
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

class DictionaryRepositoryImpl(
    private val dictionaryDao: DictionaryDao,
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

    private fun triggerSync(dictionaryId: Long) {
        val syncRequest = OneTimeWorkRequestBuilder<SyncDictionaryWorker>()
            .setInputData(
                Data.Builder()
                    .putLong(SyncDictionaryWorker.KEY_DICTIONARY_ID, dictionaryId)
                    .build()
            )
            .build()
        workManager.enqueue(syncRequest)
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

    override suspend fun syncAllToCloud() {
        val localDictionaries = allDictionaries.first()
        localDictionaries.forEach { dictionary ->
            triggerSync(dictionary.dictionaryId)
        }
    }
}