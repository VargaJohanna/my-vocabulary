package com.vocabulary.myvocabulary.room.wordData

import androidx.room.*
import io.reactivex.Single

@Dao
interface WordDao {
    @Insert
    fun insertWord(word: WordEntry)

    @Update
    fun updateWord(word: WordEntry)

    @Delete
    fun deleteWord(word: WordEntry)

    @Query("SELECT * FROM words WHERE container_dictionary_id = :dictionaryId")
    fun getAllWordsInDictionary(dictionaryId: Long): Single<List<WordEntry>>

    @Query("SELECT * FROM words WHERE word_id = :wordId")
    fun getWordById(wordId: Long): Single<WordEntry>
}