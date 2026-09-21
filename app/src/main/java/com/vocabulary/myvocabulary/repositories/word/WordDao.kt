package com.vocabulary.myvocabulary.repositories.word

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWord(word: WordEntry)

    @Update
    fun updateWord(word: WordEntry)

    @Delete
    fun deleteWord(word: WordEntry)

    @Query("SELECT * FROM words WHERE container_dictionary_id = :dictionaryId")
    fun getAllWordsInDictionary(dictionaryId: Long): Flow<List<WordEntry>>

    @Query("SELECT * FROM words WHERE word_id = :wordId")
    suspend fun getWordById(wordId: Long): WordEntry

    @Query("SELECT count(*) FROM words WHERE word_id = :wordId")
    fun getNumberOfWordById(wordId: Long): Flow<Int>
}