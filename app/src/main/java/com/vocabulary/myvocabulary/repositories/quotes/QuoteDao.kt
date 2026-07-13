package com.vocabulary.myvocabulary.repositories.quotes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuoteDao {@Query("SELECT * FROM quotes WHERE id = 0")
    suspend fun getQuote(): QuoteEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntry)
}