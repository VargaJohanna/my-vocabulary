package com.vocabulary.myvocabulary.data.dictionaryDatabase

import androidx.room.*
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary

@Dao
interface DictionaryDao {
    @Insert
    fun insertDictionary(dictionary: Dictionary)

    @Update
    fun updateDictionary(dictionary: Dictionary)

    @Delete
    fun deleteDictionary(dictionary: Dictionary)

    @Query("SELECT * FROM dictionaries")
    fun getAllDictionaries(): List<Dictionary>

    @Query("SELECT * FROM dictionaries WHERE dictionary_id = :dictionaryId")
    fun getDictionaryById(dictionaryId: Long)
}