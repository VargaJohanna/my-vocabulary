package com.vocabulary.myvocabulary.repositories.quotes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class QuoteEntry(
    @PrimaryKey val id: Int = 0,
    val quote: String,
    val author: String,
    val work: String
)