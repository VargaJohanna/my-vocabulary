package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.Quote
import io.reactivex.Single

interface NetworkQuoteRepository {
    fun fetchQuote(): Single<Quote>
}