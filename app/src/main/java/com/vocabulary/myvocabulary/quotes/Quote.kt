package com.vocabulary.myvocabulary.quotes

sealed class QuoteData {
    object EMPTY: QuoteData()

    data class Quote(val quote: String,
                     val author: String,
                     val title: String): QuoteData()

}