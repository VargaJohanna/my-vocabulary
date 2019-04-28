package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.Quote
import io.reactivex.Maybe

interface LocalQuoteRepository {
    fun getLocalQuote(): Maybe<Quote>
    fun saveLocalQuote(quote: Quote)
}