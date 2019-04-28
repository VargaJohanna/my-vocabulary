package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.network.QuoteService
import com.vocabulary.myvocabulary.quotes.Quote
import io.reactivex.Single

class NetworkQuoteRepositoryImpl(
        private val quoteService: QuoteService
) : NetworkQuoteRepository {

    override fun fetchQuote(): Single<Quote> =
            quoteService.getData().map { it.contents.quotes[0] }
}

data class ResultEntity(val contents: ContentsEntity)
data class ContentsEntity(val quotes: List<Quote>)