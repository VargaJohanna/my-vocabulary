package com.vocabulary.myvocabulary.room.dictionaryData

import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import io.reactivex.Single


interface DictionaryRepository {
    fun createDictionary(dictionary: Dictionary)
    fun deleteDictionary(dictionary: Dictionary)
    fun updateDictionary(dictionary: Dictionary)
    fun getAllDictionaries(): Single<List<Dictionary>>
    fun getDictionaryById(dictionaryId: Long): Single<Dictionary>
    fun getNumberOfDictionaries(): Single<Int>
}