package com.vocabulary.myvocabulary.repositories.sortBy

import io.reactivex.Observable

interface SortByRepository {
    val sortBy: Observable<Int>
    var sortDirection: Observable<Boolean>
    var defaultSortBy: Int
    var dateDescending: Boolean
    var wordDescending: Boolean
    var translationDescending: Boolean
    fun setSortBy(option: SortByOptions, descending: Boolean)
}