package com.vocabulary.myvocabulary.room.wordData

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.Preference
import com.vocabulary.myvocabulary.utils.SortByDirection
import com.vocabulary.myvocabulary.utils.SortByOptions
import com.vocabulary.myvocabulary.utils.toInt
import com.vocabulary.myvocabulary.utils.toSortByOption
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import com.f2prateek.rx.preferences2.RxSharedPreferences
import android.preference.PreferenceManager



class SortByRepositoryImpl(
        context: Context
) : SortByRepository {
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val rxPreferences = RxSharedPreferences.create(preferences)
    override var sortDirection: SortByDirection = SortByDirection.SortDecrease
    private var _sortBy: com.f2prateek.rx.preferences2.Preference<Int> =  rxPreferences.getInteger("SORT", 0)
    override val sortBy: Observable<Int> = _sortBy.asObservable()


    override fun setSortBy(option: SortByOptions) {
        val editor: SharedPreferences.Editor = preferences.edit()
        editor.putInt("SORT", option.toInt())
        editor.apply()
    }

    override fun reverseSortingDirection() {
        if (sortDirection == SortByDirection.SortDecrease) {
            sortDirection = SortByDirection.SortIncrease
        } else sortDirection = SortByDirection.SortDecrease
    }
}