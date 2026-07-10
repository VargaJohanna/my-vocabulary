package com.vocabulary.myvocabulary.quotes

sealed class QuoteData {
    object EMPTY: QuoteData()

    data class Quote(
        val quote: String,
        val author: String,
        val work: String,
        val categories: List<String> = emptyList()
    ): QuoteData()

}