package com.vocabulary.myvocabulary.repositories.sortBy

sealed class SortByOptions(
        val id: Int
) {
    object SortByTranslation : SortByOptions(0)
    object SortByWord : SortByOptions(1)
    object SortByDate : SortByOptions(2)
}

fun Int.toSortByOption(): SortByOptions {
    return when(this) {
        SortByOptions.SortByTranslation.id -> SortByOptions.SortByTranslation
        SortByOptions.SortByWord.id -> SortByOptions.SortByWord
        SortByOptions.SortByDate.id -> SortByOptions.SortByDate
        else -> throw IllegalStateException("Unknown sort by option: $this")
    }
}

fun SortByOptions.toInt(): Int {
    return id
}

