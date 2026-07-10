package com.vocabulary.myvocabulary.repositories.quotes

import com.vocabulary.myvocabulary.quotes.QuoteData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class QuoteRepositoryImpl(
    private val networkQuoteRepository: NetworkQuoteRepository,
    private val localQuoteRepository: LocalQuoteRepository
) : QuoteRepository {

    override fun getQuote(): Flow<QuoteData.Quote> = flow<QuoteData.Quote> {
        try {
            val local = localQuoteRepository.getQuote()
            local?.let {
                emit(it)
            }
        } catch (e: Exception) {
            println("Local fetch failed: ${e.message}")
        }

        try {
            val network = networkQuoteRepository.fetchQuote()
            localQuoteRepository.saveQuote(network)
            emit(network)
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}