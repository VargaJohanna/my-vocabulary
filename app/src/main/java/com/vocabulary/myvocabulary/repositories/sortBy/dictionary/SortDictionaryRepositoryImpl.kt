package com.vocabulary.myvocabulary.repositories.sortBy.dictionary

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import io.reactivex.Observable
import io.reactivex.functions.Function3

class SortDictionaryRepositoryImpl(
        private val preferences: SharedPreferences,
        rxPreferences: RxSharedPreferences
) : SortDictionaryRepository {
    private val sortDateDirection: Observable<Boolean> = rxPreferences.getBoolean(SORT_DICT_DATE_DIRECTION_KEY, true).asObservable()
    private val sortTitleDirection: Observable<Boolean> = rxPreferences.getBoolean(SORT_DICT_TITLE_DIRECTION_KEY, true).asObservable()
    private val sortBy: Observable<Int> = rxPreferences.getInteger(SORT_DICT_KEY, 1).asObservable()

    override fun setSortBy(sortByData: SortDictionaryData) {
        preferences.edit().apply {
            putInt(SORT_DICT_KEY, sortByData.sortByOption.toInt())
            when (sortByData.sortByOption) {
                SortByDictionaryOptions.SortByDate -> {
                    putBoolean(SORT_DICT_DATE_DIRECTION_KEY, sortByData.dateDescending)
                }
                SortByDictionaryOptions.SortByTitle -> {
                    putBoolean(SORT_DICT_TITLE_DIRECTION_KEY, sortByData.titleDescending)
                }
            }
            apply()
        }
    }

    override fun sortByData(): Observable<SortDictionaryData> {
        return Observable.combineLatest(
                sortBy,
                sortDateDirection,
                sortTitleDirection,
                Function3 { sortByOption, date, title ->
                    SortDictionaryData(
                            sortByOption = sortByOption.toSortByDictionaryOption(),
                            dateDescending = date,
                            titleDescending = title)
                })
    }

    companion object {
        const val SORT_DICT_KEY = "SORT_DICT_KEY"
        const val SORT_DICT_TITLE_DIRECTION_KEY = "SORT_DICT_TITLE_DIRECTION_KEY"
        const val SORT_DICT_DATE_DIRECTION_KEY = "SORT_DICT_DATE_DIRECTION_KEY"
    }
}