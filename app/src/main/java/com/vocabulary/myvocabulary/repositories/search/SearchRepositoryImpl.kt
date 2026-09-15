package com.vocabulary.myvocabulary.repositories.search

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchRepositoryImpl : SearchRepository {
    private val _searchBarState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val searchBarState: Flow<Boolean> = _searchBarState.asStateFlow()
    private val _searchedTerm: MutableStateFlow<String> = MutableStateFlow("")
    override val searchedTerm: Flow<String> = _searchedTerm.asStateFlow()

    override fun setSearchedTerm(search: String) {
        _searchedTerm.value = search
    }

    override fun saveSearchBarStatus(isSearchOpen: Boolean) {
        _searchBarState.value = isSearchOpen
    }

    override fun showSearchBar(): Flow<Boolean> {
        return searchBarState
    }
}