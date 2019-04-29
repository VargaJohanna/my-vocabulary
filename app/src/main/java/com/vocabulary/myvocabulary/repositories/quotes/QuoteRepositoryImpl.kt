package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.QuoteData
import io.reactivex.Observable

class QuoteRepositoryImpl(
        private val networkQuoteRepository: NetworkQuoteRepository,
        private val localQuoteRepository: LocalQuoteRepository
) : QuoteRepository {

    override fun getQuote(): Observable<QuoteData.Quote> {
        val localQuote: Observable<QuoteData.Quote> = localQuoteRepository.getQuote().toObservable()
        val networkQuote: Observable<QuoteData.Quote> = networkQuoteRepository.fetchQuote().toObservable()

        return Observable.concat(
                localQuote,
                networkQuote.doOnNext {
                    localQuoteRepository.saveQuote(it)
                })
    }
}