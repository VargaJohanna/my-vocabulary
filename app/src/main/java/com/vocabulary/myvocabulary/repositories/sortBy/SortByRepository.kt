package com.vocabulary.myvocabulary.repositories.sortBy

import kotlinx.coroutines.flow.Flow

interface SortByRepository {
    fun setSortBy(sortByData: SortByData)
    fun sortByData(): Flow<SortByData>
}