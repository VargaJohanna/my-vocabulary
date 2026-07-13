package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.QuoteData

class LocalQuoteRepositoryImpl(
    private val quoteDao: QuoteDao
) : LocalQuoteRepository {
    override suspend fun getQuote(): QuoteData.Quote? {
        return quoteDao.getQuote()?.let{
            QuoteData.Quote(
                quote = it.quote,
                author = it.author,
                work = it.work
            )
        }
    }

    override suspend fun saveQuote(quote: QuoteData.Quote) {
        quoteDao.insertQuote(
            QuoteEntry(
                quote = quote.quote,
                author = quote.author,
                work = quote.work
            )
        )
    }
}