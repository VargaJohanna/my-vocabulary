package com.vocabulary.myvocabulary.utils

sealed class SortByOptions {
    object SortByTranslation : SortByOptions()
    object SortByWord : SortByOptions()
    object SortByDate : SortByOptions()
}

