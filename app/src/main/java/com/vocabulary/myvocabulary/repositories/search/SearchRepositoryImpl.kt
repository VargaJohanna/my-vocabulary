package com.vocabulary.myvocabulary.repositories.search

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchRepositoryImpl : SearchRepository {
    private val _searchBarState: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val searchBarState: Observable<Boolean> = _searchBarState
    private val _searchedTerm: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    override val searchedTerm: Observable<String> = _searchedTerm

    private val _searchBarStateFlow = MutableStateFlow(false)
    val searchBarStateFlow = _searchBarStateFlow.asStateFlow()

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