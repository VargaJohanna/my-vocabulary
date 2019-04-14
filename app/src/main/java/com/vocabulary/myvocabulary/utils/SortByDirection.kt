//package com.vocabulary.myvocabulary.utils
//
//sealed class SortByDirection {
//    object SortDecrease: SortByDirection()
//    object SortIncrease: SortByDirection()
//}
//
//fun Int.toSortByDirection(): SortByDirection {
//    return when(this) {
//        0 -> SortByDirection.SortDecrease
//        1 -> SortByDirection.SortIncrease
//        else -> throw IllegalStateException("Unknown sort by option: $this")
//    }
//}
//
//fun SortByDirection.toInt(): Int {
//    return when(this) {
//        SortByDirection.SortDecrease -> 0
//        SortByDirection.SortIncrease -> 1
//    }
//}