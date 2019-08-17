package com.vocabulary.myvocabulary.repositories.search

import io.reactivex.Observable

interface SearchRepository {
    fun saveSearchBarStatus(isSearchOpen: Boolean)
    fun showSearchBar() : Observable<Boolean>
}