package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.QuoteData

interface LocalQuoteRepository {
    suspend fun getQuote(): QuoteData.Quote?
    suspend fun saveQuote(quote: QuoteData.Quote)
}