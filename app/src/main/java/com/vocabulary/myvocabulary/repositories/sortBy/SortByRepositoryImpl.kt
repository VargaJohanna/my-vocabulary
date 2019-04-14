package com.vocabulary.myvocabulary.repositories.sortBy

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import io.reactivex.Observable
import io.reactivex.functions.Function4


class SortByRepositoryImpl(
        private val preferences: SharedPreferences,
        rxPreferences: RxSharedPreferences
) : SortByRepository {
    private val sortDateDirection: Observable<Boolean> = rxPreferences.getBoolean(SORT_DATE_DIRECTION_KEY, true).asObservable()
    private val sortWordDirection: Observable<Boolean> = rxPreferences.getBoolean(SORT_WORD_DIRECTION_KEY, false).asObservable()
    private val sortTranslationDirection: Observable<Boolean> = rxPreferences.getBoolean(SORT_TRANSLATION_DIRECTION_KEY, false).asObservable()
    private val sortBy: Observable<Int> = rxPreferences.getInteger(SORT_KEY, 2).asObservable()

    override fun setSortBy(sortByData: SortByData) {
        preferences.edit().apply {
            putInt(SORT_KEY, sortByData.sortByOption.toInt())
            when (sortByData.sortByOption) {
                SortByOptions.SortByDate -> {
                    putBoolean(SORT_DATE_DIRECTION_KEY, sortByData.dateDescending)
                }
                SortByOptions.SortByWord -> {
                    putBoolean(SORT_WORD_DIRECTION_KEY, sortByData.wordDescending)
                }
                SortByOptions.SortByTranslation -> {
                    putBoolean(SORT_TRANSLATION_DIRECTION_KEY, sortByData.translationDescending)
                }
            }
            apply()
        }
        sortByData()
    }

    override fun sortByData(): Observable<SortByData> {
        return Observable.combineLatest(
                sortBy,
                sortDateDirection,
                sortWordDirection,
                sortTranslationDirection,
                Function4 { sortByOption, date, word, translation ->
                    SortByData(
                            sortByOption = sortByOption.toSortByOption(),
                            wordDescending = word,
                            dateDescending = date,
                            translationDescending = translation)
                })
    }

    companion object {
        const val SORT_KEY = "SORT"
        const val SORT_WORD_DIRECTION_KEY = "SORT_WORD_DIRECTION"
        const val SORT_TRANSLATION_DIRECTION_KEY = "SORT_TRANSLATION_DIRECTION"
        const val SORT_DATE_DIRECTION_KEY = "SORT_DATE_DIRECTION"
    }
}