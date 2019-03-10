package com.vocabulary.myvocabulary.room.dictionaryData

import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import io.reactivex.Single

class DictionaryRepositoryImpl(
        private val dictionaryDao: DictionaryDao
) : DictionaryRepository {
    override fun getNumberOfDictionaries() = dictionaryDao.getNumberOfDictionaries()

    override fun createDictionary(dictionary: Dictionary): Long {
        return dictionaryDao.insertDictionary(dictionary.toDictionaryEntry())
    }

    override fun deleteDictionary(dictionary: Dictionary) = dictionaryDao.deleteDictionary(dictionary.toDictionaryEntry())

    override fun updateDictionary(dictionary: Dictionary) = dictionaryDao.updateDictionary(dictionary.toDictionaryEntry())

    override fun getAllDictionaries(): Single<List<Dictionary>> {
        return dictionaryDao.getAllDictionaries().map { list ->
            list.map {
                it.toDictionary()
            }
        }
    }

    override fun getDictionaryById(dictionaryId: Long): Single<Dictionary> {
        return dictionaryDao.getDictionaryById(dictionaryId).map {
            it.toDictionary()
        }
    }
}