package com.vocabulary.myvocabulary.repositories.sortBy

import io.reactivex.Observable

interface SortByRepository {
    fun setSortBy(sortByData: SortByData)
    fun sortByData(): Observable<SortByData>
}