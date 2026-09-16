package com.vocabulary.myvocabulary.repositories.sortBy.dictionary

import kotlinx.coroutines.flow.Flow

interface SortDictionaryRepository {
    fun setSortBy(sortByData: SortDictionaryData)
    fun sortByData(): Flow<SortDictionaryData>
}