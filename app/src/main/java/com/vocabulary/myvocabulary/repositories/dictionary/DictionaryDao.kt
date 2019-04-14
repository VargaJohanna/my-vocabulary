package com.vocabulary.myvocabulary.repositories.dictionary

import androidx.room.*
import io.reactivex.Observable

@Dao
interface DictionaryDao {
    @Insert
    fun insertDictionary(dictionaryEntry: DictionaryEntry): Long

    @Update
    fun updateDictionary(dictionaryEntry: DictionaryEntry)

    @Delete
    fun deleteDictionary(dictionaryEntry: DictionaryEntry)

    @Query("SELECT * FROM dictionaries")
    fun getAllDictionaries(): Observable<List<DictionaryEntry>>

    @Query("SELECT count(*) FROM dictionaries")
    fun getNumberOfDictionaries(): Observable<Int>
}