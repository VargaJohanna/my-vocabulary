package com.vocabulary.myvocabulary.room.dictionaryData

import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import io.reactivex.Observable


interface DictionaryRepository {
    fun createDictionary(dictionary: Dictionary)
    fun deleteDictionary(dictionary: Dictionary)
    fun updateDictionary(dictionary: Dictionary)
    fun getAllDictionaries(): Observable<List<Dictionary>>
    fun getDictionaryById(dictionaryId: Long): Observable<Dictionary>
    fun getNumberOfDictionaries(): Observable<Int>
}