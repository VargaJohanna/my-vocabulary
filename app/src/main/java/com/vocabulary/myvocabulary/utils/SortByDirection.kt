package com.vocabulary.myvocabulary.utils

sealed class SortByDirection{
    object SortDecrease: SortByDirection()
    object SortIncrease: SortByDirection()
}