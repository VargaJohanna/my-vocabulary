package com.vocabulary.myvocabulary.repositories.sortBy.dictionary

sealed class SortByDictionaryOptions(
        val id: Int
) {
    object SortByDate : SortByDictionaryOptions(0)
    object SortByTitle : SortByDictionaryOptions(1)
}

fun Int.toSortByDictionaryOption(): SortByDictionaryOptions {
    return when (this) {
        SortByDictionaryOptions.SortByDate.id -> SortByDictionaryOptions.SortByDate
        SortByDictionaryOptions.SortByTitle.id -> SortByDictionaryOptions.SortByTitle
        else -> throw IllegalStateException("Unknown sort by option: $this")
    }
}

fun SortByDictionaryOptions.toInt(): Int {
    return id
}