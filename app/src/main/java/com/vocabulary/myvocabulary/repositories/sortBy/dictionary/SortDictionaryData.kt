package com.vocabulary.myvocabulary.repositories.sortBy.dictionary

data class SortDictionaryData(
        val sortByOption: SortByDictionaryOptions = SortByDictionaryOptions.SortByDate,
        val dateDescending: Boolean = true,
        val titleDescending: Boolean = true
)