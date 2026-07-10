package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.QuoteData

interface NetworkQuoteRepository {
    suspend fun fetchQuote(): QuoteData.Quote
}