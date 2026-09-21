package com.vocabulary.myvocabulary.repositories.dictionary

import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import kotlinx.coroutines.flow.Flow

interface  DictionaryRepository {
    val allDictionaries: Flow<List<Dictionary>>
    val numberOfDictionaries: Flow<Int>
    suspend fun createDictionary(dictionary: Dictionary): Long
    suspend fun deleteDictionary(dictionary: Dictionary)
    suspend fun updateDictionary(dictionary: Dictionary)
    suspend fun getDictionaryById(dictionaryId: Long): Dictionary
    suspend fun onQuizFinished(dictionaryId: Long?)
    suspend fun saveQuizStats(id: Long, scorePercentage: Int)
    suspend fun syncAllToCloud(requireWifi: Boolean = false)
    suspend fun syncFromCloud(userId: String, requireWifi: Boolean = false): Result<Unit>
    suspend fun clearLocalData()
}