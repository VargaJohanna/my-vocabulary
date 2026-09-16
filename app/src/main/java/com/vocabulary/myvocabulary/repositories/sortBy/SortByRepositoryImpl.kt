package com.vocabulary.myvocabulary.repositories.sortBy

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SortByRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope
) : SortByRepository {

    override fun setSortBy(sortByData: SortByData) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[SORT_KEY] = sortByData.sortByOption.toInt()
                when (sortByData.sortByOption) {
                    SortByOptions.SortByDate -> {
                        preferences[SORT_DATE_DIRECTION_KEY] = sortByData.dateDescending
                    }
                    SortByOptions.SortByWord -> {
                        preferences[SORT_WORD_DIRECTION_KEY] = sortByData.wordDescending
                    }
                    SortByOptions.SortByTranslation -> {
                        preferences[SORT_TRANSLATION_DIRECTION_KEY] = sortByData.translationDescending
                    }
                }
            }
        }
    }

    override fun sortByData(): Flow<SortByData> {
        return combine(
            dataStore.data.map { it[SORT_KEY] ?: 2 },
            dataStore.data.map { it[SORT_DATE_DIRECTION_KEY] ?: true },
            dataStore.data.map { it[SORT_WORD_DIRECTION_KEY] ?: true },
            dataStore.data.map { it[SORT_TRANSLATION_DIRECTION_KEY] ?: true }
        ) { sortByOption, date, word, translation ->
            SortByData(
                sortByOption = sortByOption.toSortByOption(),
                wordDescending = word,
                dateDescending = date,
                translationDescending = translation
            )
        }
    }

    companion object {
        val SORT_KEY = intPreferencesKey("SORT")
        val SORT_WORD_DIRECTION_KEY = booleanPreferencesKey("SORT_WORD_DIRECTION")
        val SORT_TRANSLATION_DIRECTION_KEY = booleanPreferencesKey("SORT_TRANSLATION_DIRECTION")
        val SORT_DATE_DIRECTION_KEY = booleanPreferencesKey("SORT_DATE_DIRECTION")
    }
}
