package com.vocabulary.myvocabulary.repositories.dictionary

import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

class DictionaryRepositoryImpl(
    private val dictionaryDao: DictionaryDao,

) : DictionaryRepository {
    override val allDictionaries: Flow<List<Dictionary>> = dictionaryDao.getAllDictionaries()
        .map { list ->
            list.map { it.toDictionary() }
        }
    override val numberOfDictionaries: Flow<Int> = allDictionaries.map { it.size }

    override suspend fun createDictionary(dictionary: Dictionary) =
        dictionaryDao.insertDictionary(dictionary.toDictionaryEntry())

    override suspend fun deleteDictionary(dictionary: Dictionary) =
        dictionaryDao.deleteDictionary(dictionary.toDictionaryEntry())

    override suspend fun updateDictionary(dictionary: Dictionary) =
        dictionaryDao.updateDictionary(dictionary.toDictionaryEntry())

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
}