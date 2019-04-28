package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.QuoteData
import io.reactivex.Maybe

interface LocalQuoteRepository {
    fun getLocalQuote(): Maybe<QuoteData.Quote>
    fun saveLocalQuote(quote: QuoteData.Quote)
}