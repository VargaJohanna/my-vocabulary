package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.QuoteData
import io.reactivex.Maybe

interface LocalQuoteRepository {
    fun getQuote(): Maybe<QuoteData.Quote>
    fun saveQuote(quote: QuoteData.Quote)
}