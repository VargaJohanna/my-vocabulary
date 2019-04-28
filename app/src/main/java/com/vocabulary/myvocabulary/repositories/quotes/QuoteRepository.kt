package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.Quote
import io.reactivex.Observable
import io.reactivex.Single

interface QuoteRepository {
    fun getQuote(): Observable<Quote>
}