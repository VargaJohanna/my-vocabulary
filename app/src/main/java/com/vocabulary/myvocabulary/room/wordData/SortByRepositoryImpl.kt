package com.vocabulary.myvocabulary.room.wordData

import com.vocabulary.myvocabulary.utils.SortByOptions
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class SortByRepositoryImpl: SortByRepository {
    private val _sortBy = BehaviorSubject.createDefault<SortByOptions>(SortByOptions.SortByDate)
    override val sortBy: Observable<SortByOptions> = _sortBy

    override fun setSortBy(option: SortByOptions) {
        _sortBy.onNext(option)
    }
}