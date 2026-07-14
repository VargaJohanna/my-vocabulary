package com.vocabulary.myvocabulary.network

import com.vocabulary.myvocabulary.quotes.QuoteData
import retrofit2.http.GET

interface QuoteService {

    @GET("quoteoftheday?category=happiness")
    suspend fun getData(): List<QuoteData.Quote>
}