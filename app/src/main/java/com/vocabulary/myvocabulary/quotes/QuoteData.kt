package com.vocabulary.myvocabulary.quotes

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

sealed class QuoteData {
    object EMPTY: QuoteData()

    @Keep
    data class Quote(
        @SerializedName("quote") val quote: String = "",
        @SerializedName("author") val author: String = "",
        @SerializedName("work") val work: String = "",
        @SerializedName("categories") val categories: List<String> = emptyList()
    ): QuoteData()
}