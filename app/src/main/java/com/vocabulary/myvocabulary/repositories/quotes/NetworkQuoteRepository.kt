package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.QuoteData
import io.reactivex.Single

interface NetworkQuoteRepository {
    fun fetchQuote(): Single<QuoteData.Quote>
}