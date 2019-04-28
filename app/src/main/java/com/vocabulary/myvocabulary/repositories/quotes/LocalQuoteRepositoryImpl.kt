package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.Quote
import io.reactivex.Maybe

class LocalQuoteRepositoryImpl : LocalQuoteRepository {
    private var localQuote: Quote? = null

    override fun getLocalQuote(): Maybe<Quote> {
        return if (localQuote == null) Maybe.empty() else Maybe.just(localQuote)
    }

    override fun saveLocalQuote(quote: Quote) {
        localQuote = quote
    }
}