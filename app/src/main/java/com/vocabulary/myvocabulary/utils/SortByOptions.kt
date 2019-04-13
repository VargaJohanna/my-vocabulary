package com.vocabulary.myvocabulary.utils

sealed class SortByOptions {
    object SortByTranslation : SortByOptions()
    object SortByWord : SortByOptions()
    object SortByDate : SortByOptions()
}

fun Int.toSortByOption(): SortByOptions {
    return when(this) {
        0 -> SortByOptions.SortByTranslation
        1 -> SortByOptions.SortByWord
        2 -> SortByOptions.SortByDate
        else -> throw IllegalStateException("Unknown sort by option: $this")
    }
}

fun SortByOptions.toInt(): Int {
    return when(this) {
        SortByOptions.SortByTranslation -> 0
        SortByOptions.SortByWord -> 1
        SortByOptions.SortByDate -> 2
    }
}

