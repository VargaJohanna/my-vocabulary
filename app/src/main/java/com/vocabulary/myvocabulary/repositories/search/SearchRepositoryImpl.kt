package com.vocabulary.myvocabulary.repositories.search

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class SearchRepositoryImpl : SearchRepository {
    private val _searchBarState: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val searchBarState: Observable<Boolean> = _searchBarState

    override fun saveSearchBarStatus(isSearchOpen: Boolean) {
        _searchBarState.onNext(isSearchOpen)
    }

    override fun showSearchBar(): Observable<Boolean> {
        return searchBarState
    }
}