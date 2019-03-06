package com.vocabulary.myvocabulary.room.dictionaryData

import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import io.reactivex.Observable

class DictionaryRepositoryImpl(
        private val dictionaryDao: DictionaryDao
) : DictionaryRepository {
    override fun createDictionary(dictionary: Dictionary) {
        dictionaryDao.insertDictionary(dictionary.toDictionaryEntry())
    }

    override fun deleteDictionary(dictionary: Dictionary) {
        dictionaryDao.deleteDictionary(dictionary.toDictionaryEntry())
    }

    override fun updateDictionary(dictionary: Dictionary) {
        dictionaryDao.updateDictionary(dictionary.toDictionaryEntry())
    }

    override fun getAllDictionaries(): Observable<List<Dictionary>> {
        return dictionaryDao.getAllDictionaries().map { list ->
            list.map {
                it.toDictionary()
            }
        }
    }

    override fun getDictionaryById(dictionaryId: Long): Observable<Dictionary> {
        return dictionaryDao.getDictionaryById(dictionaryId).map {
            it.toDictionary()
        }
    }
}