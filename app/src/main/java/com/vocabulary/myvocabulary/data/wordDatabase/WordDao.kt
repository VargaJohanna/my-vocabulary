package com.vocabulary.myvocabulary.data.wordDatabase

import androidx.room.*
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.words.Word

@Dao
interface WordDao {
    @Insert
    fun insertWord(word: Word)

    @Update
    fun updateWord(word: Word)

    @Delete
    fun deleteWord(word: Word)

    @Query("SELECT * FROM words WHERE container_dictionary_id = :dictionaryId")
    fun getAllWordsInDictionary(dictionaryId: Long): List<Dictionary>

    @Query("SELECT * FROM words WHERE word_id = :wordId")
    fun getWordById(wordId: Long)
}