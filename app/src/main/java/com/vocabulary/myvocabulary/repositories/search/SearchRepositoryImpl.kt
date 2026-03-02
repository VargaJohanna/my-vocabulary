package com.vocabulary.myvocabulary.repositories.search

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class SearchRepositoryImpl : SearchRepository {
    private val _searchBarState: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val searchBarState: Observable<Boolean> = _searchBarState
    private val _searchedTerm: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    override val searchedTerm: Observable<String> = _searchedTerm

    override fun setSearchedTerm(search: String) {
        _searchedTerm.onNext(search)
    }

    override fun saveSearchBarStatus(isSearchOpen: Boolean) {
        _searchBarState.onNext(isSearchOpen)
    }

    override fun showSearchBar(): Observable<Boolean> {
        return searchBarState
    }
}