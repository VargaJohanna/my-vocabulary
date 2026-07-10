package com.vocabulary.myvocabulary.network

import com.vocabulary.myvocabulary.quotes.QuoteData
import retrofit2.http.GET

interface QuoteService {

    @GET("quoteoftheday?category=happiness&categories=truth%2Cwisdom")
    suspend fun getData(): List<QuoteData.Quote>
}