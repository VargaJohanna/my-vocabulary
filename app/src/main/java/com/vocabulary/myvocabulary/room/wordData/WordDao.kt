package com.vocabulary.myvocabulary.room.wordData

import androidx.room.*
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryEntry
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.words.Word

@Dao
interface WordDao {
    @Insert
    fun insertWord(word: WordEntry)

    @Update
    fun updateWord(word: WordEntry)

    @Delete
    fun deleteWord(word: WordEntry)

    @Query("SELECT * FROM words WHERE container_dictionary_id = :dictionaryId")
    fun getAllWordsInDictionary(dictionaryId: Long): List<WordEntry>

    @Query("SELECT * FROM words WHERE word_id = :wordId")
    fun getWordById(wordId: Long): WordEntry
}