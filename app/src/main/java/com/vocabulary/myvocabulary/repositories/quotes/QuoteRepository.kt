package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.QuoteData
import io.reactivex.Observable

interface QuoteRepository {
    fun getQuote(): Observable<QuoteData.Quote>
}