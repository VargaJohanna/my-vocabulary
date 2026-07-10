package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.QuoteData

class LocalQuoteRepositoryImpl : LocalQuoteRepository {
    private var localQuote: QuoteData.Quote? = null

    override suspend fun getQuote(): QuoteData.Quote? {
        return localQuote
    }

    override suspend fun saveQuote(quote: QuoteData.Quote) {
        localQuote = quote
    }
}