package com.vocabulary.myvocabulary.network

import com.vocabulary.myvocabulary.repositories.quotes.ResultEntity
import io.reactivex.Single
import retrofit2.http.GET

interface QuoteService {

    @GET("/qod?category=inspire")
    fun getData(): Single<ResultEntity>
}