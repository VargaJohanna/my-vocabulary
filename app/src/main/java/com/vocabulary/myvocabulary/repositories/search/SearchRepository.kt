package com.vocabulary.myvocabulary.repositories.search

import io.reactivex.Observable

interface SearchRepository {
    val searchedTerm: Observable<String>
    fun setSearchedTerm(search: String)
    fun saveSearchBarStatus(isSearchOpen: Boolean)
    fun showSearchBar(): Observable<Boolean>
}