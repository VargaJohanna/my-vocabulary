package com.vocabulary.myvocabulary.repositories.sortBy

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import io.reactivex.Observable


class SortByRepositoryImpl(
        context: Context
) : SortByRepository {
    override var wordDescending: Boolean = false
    override var translationDescending: Boolean = false
    override var dateDescending: Boolean = true
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val rxPreferences = RxSharedPreferences.create(preferences)
    override var sortDirection: Observable<Boolean> = rxPreferences.getBoolean(SORT_DIRECTION_KEY, false).asObservable()
    override var defaultSortBy: Int = 2
    override val sortBy: Observable<Int> = rxPreferences.getInteger(SORT_KEY, defaultSortBy).asObservable()

    override fun setSortBy(option: SortByOptions, descending: Boolean) {
        defaultSortBy = option.toInt()
        preferences.edit().apply {
            putInt(SORT_KEY, option.toInt())
            putBoolean(SORT_DIRECTION_KEY, descending)
            apply()
        }
    }

    companion object {
        const val SORT_KEY = "SORT"
        const val SORT_DIRECTION_KEY = "SORT_DIRECTION"
    }
}