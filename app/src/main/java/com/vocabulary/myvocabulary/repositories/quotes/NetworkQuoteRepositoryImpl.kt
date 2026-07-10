package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.network.QuoteService
import com.vocabulary.myvocabulary.quotes.QuoteData

class NetworkQuoteRepositoryImpl(
    private val quoteService: QuoteService
) : NetworkQuoteRepository {

    override suspend fun fetchQuote(): QuoteData.Quote {
        val response = quoteService.getData()

        return response.firstOrNull() ?: throw Exception("No quotes found in network response")
    }
}