package com.vocabulary.myvocabulary.room.wordData

import com.vocabulary.myvocabulary.utils.SortByOptions
import io.reactivex.Observable

interface SortByRepository {
    val sortBy: Observable<SortByOptions>
    fun setSortBy(option: SortByOptions)
}