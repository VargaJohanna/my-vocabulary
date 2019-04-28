package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.QuoteData
import io.reactivex.Maybe

class LocalQuoteRepositoryImpl : LocalQuoteRepository {
    private var localQuote: QuoteData.Quote? = null

    override fun getLocalQuote(): Maybe<QuoteData.Quote> {
        return if (localQuote == null) Maybe.empty() else Maybe.just(localQuote)
    }

    override fun saveLocalQuote(quote: QuoteData.Quote) {
        localQuote = quote
    }
}