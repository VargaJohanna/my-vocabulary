package com.vocabulary.myvocabulary.repositories.dictionary

import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import io.reactivex.Observable
import io.reactivex.Single


interface  DictionaryRepository {
    val allDictionaries: Observable<List<Dictionary>>
    val numberOfDictionaries: Observable<Int>
    fun createDictionary(dictionary: Dictionary): Long
    fun deleteDictionary(dictionary: Dictionary)
    fun updateDictionary(dictionary: Dictionary)
    fun getDictionaryById(dictionaryId: Long): Single<Dictionary>
}