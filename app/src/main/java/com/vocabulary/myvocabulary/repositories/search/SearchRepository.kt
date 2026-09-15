package com.vocabulary.myvocabulary.repositories.search

import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    val searchedTerm: Flow<String>
    fun setSearchedTerm(search: String)
    fun saveSearchBarStatus(isSearchOpen: Boolean)
    fun showSearchBar(): Flow<Boolean>
}