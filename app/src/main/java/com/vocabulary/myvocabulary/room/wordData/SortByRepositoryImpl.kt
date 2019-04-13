package com.vocabulary.myvocabulary.room.wordData

import com.vocabulary.myvocabulary.utils.SortByDirection
import com.vocabulary.myvocabulary.utils.SortByOptions
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class SortByRepositoryImpl: SortByRepository {
    override var sortDirection: SortByDirection = SortByDirection.SortDecrease
    private val _sortBy = BehaviorSubject.createDefault<SortByOptions>(SortByOptions.SortByDate)
    override val sortBy: Observable<SortByOptions> = _sortBy

    override fun setSortBy(option: SortByOptions) {
        _sortBy.onNext(option)
    }

    override fun reverseSortingDirection() {
        if(sortDirection == SortByDirection.SortDecrease) { sortDirection = SortByDirection.SortIncrease }
        else sortDirection = SortByDirection.SortDecrease
    }
}