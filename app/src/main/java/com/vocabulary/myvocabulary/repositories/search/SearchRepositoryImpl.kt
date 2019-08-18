package com.vocabulary.myvocabulary.repositories.search

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import io.reactivex.Observable

class SearchRepositoryImpl(
        private val preferences: SharedPreferences,
        rxPreferences: RxSharedPreferences
) : SearchRepository {
    private val searchBarStatus: Observable<Boolean> = rxPreferences.getBoolean(SEARCH_BAR_KEY, false).asObservable()

    override fun saveSearchBarStatus(isSearchOpen: Boolean) {
        preferences.edit().apply{
            putBoolean(SEARCH_BAR_KEY, isSearchOpen)
            apply()
        }
    }

    override fun showSearchBar(): Observable<Boolean> {
        return searchBarStatus
    }

    companion object {
        const val SEARCH_BAR_KEY = "SEARCH"
    }
}