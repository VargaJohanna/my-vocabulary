package com.vocabulary.myvocabulary.repositories.sortBy

data class SortByData(
        val sortByOption: SortByOptions = SortByOptions.SortByDate,
        val dateDescending: Boolean = true,
        val wordDescending: Boolean = true,
        val translationDescending: Boolean = true
)