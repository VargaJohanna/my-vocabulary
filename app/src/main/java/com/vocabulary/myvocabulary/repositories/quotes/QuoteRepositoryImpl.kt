package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.Quote
import io.reactivex.Observable

class QuoteRepositoryImpl(
        private val networkQuoteRepository: NetworkQuoteRepository,
        private val localQuoteRepository: LocalQuoteRepository
) : QuoteRepository {

    override fun getQuote(): Observable<Quote> {
        val localQuote: Observable<Quote> = localQuoteRepository.getLocalQuote().toObservable()
        val networkQuote: Observable<Quote> = networkQuoteRepository.fetchQuote().toObservable()

        return Observable.concat(localQuote, networkQuote
                .doOnNext {
                    localQuoteRepository.saveLocalQuote(it)
                })

    }
}