package com.vocabulary.myvocabulary.repositories.sortBy.dictionary

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

class SortDictionaryRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope
) : SortDictionaryRepository {

    override fun setSortBy(sortByData: SortDictionaryData) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[SORT_DICT_KEY] = sortByData.sortByOption.toInt()
                when (sortByData.sortByOption) {
                    SortByDictionaryOptions.SortByDate -> {
                        preferences[SORT_DICT_DATE_DIRECTION_KEY] = sortByData.dateDescending
                    }
                    SortByDictionaryOptions.SortByTitle -> {
                        preferences[SORT_DICT_TITLE_DIRECTION_KEY] = sortByData.titleDescending
                    }
                }
            }
        }
    }

    override fun sortByData(): Flow<SortDictionaryData> {
        return combine(
            dataStore.data.map { it[SORT_DICT_KEY] ?: 0 },
            dataStore.data.map { it[SORT_DICT_DATE_DIRECTION_KEY] ?: true },
            dataStore.data.map { it[SORT_DICT_TITLE_DIRECTION_KEY] ?: true }
        ) { sortByOption, date, title ->
            SortDictionaryData(
                sortByOption = sortByOption.toSortByDictionaryOption(),
                dateDescending = date,
                titleDescending = title
            )
        }
    }

    companion object {
        val SORT_DICT_KEY = intPreferencesKey("SORT_DICT_KEY")
        val SORT_DICT_TITLE_DIRECTION_KEY = booleanPreferencesKey("SORT_DICT_TITLE_DIRECTION_KEY")
        val SORT_DICT_DATE_DIRECTION_KEY = booleanPreferencesKey("SORT_DICT_DATE_DIRECTION_KEY")
    }
}
