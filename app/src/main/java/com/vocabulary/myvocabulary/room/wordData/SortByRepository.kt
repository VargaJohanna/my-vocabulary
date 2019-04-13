package com.vocabulary.myvocabulary.room.wordData

import com.vocabulary.myvocabulary.utils.SortByDirection
import com.vocabulary.myvocabulary.utils.SortByOptions
import io.reactivex.Observable

interface SortByRepository {
    val sortBy: Observable<Int>
    var sortDirection: SortByDirection
    fun setSortBy(option: SortByOptions)
    fun reverseSortingDirection()
}