package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.QuoteData
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    fun getQuote(): Flow<QuoteData.Quote>
}