package com.vocabulary.myvocabulary.room.wordData

import com.vocabulary.myvocabulary.utils.SortByOptions
import io.reactivex.Observable

interface SortByRepository {
    val sortBy: Observable<Int>
    var sortDirection: Observable<Boolean>
    var dateDescending: Boolean
    var wordDescending: Boolean
    var translationDescending: Boolean
    fun setSortBy(option: SortByOptions, descending: Boolean)
}