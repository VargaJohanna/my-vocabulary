package com.vocabulary.myvocabulary.repositories.sortBy.dictionary

import io.reactivex.Observable

interface SortDictionaryRepository {
    fun setSortBy(sortByData: SortDictionaryData)
    fun sortByData(): Observable<SortDictionaryData>
}