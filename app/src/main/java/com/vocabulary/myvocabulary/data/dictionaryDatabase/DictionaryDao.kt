package com.vocabulary.myvocabulary.data.dictionaryDatabase

import androidx.room.*
import io.reactivex.Observable

@Dao
interface DictionaryDao {
    @Insert
    fun insertDictionary(dictionary: DictionaryEntry)

    @Update
    fun updateDictionary(dictionary: DictionaryEntry)

    @Delete
    fun deleteDictionary(dictionary: DictionaryEntry)

    @Query("SELECT * FROM dictionaries")
    fun getAllDictionaries(): Observable<List<DictionaryEntry>>

    @Query("SELECT * FROM dictionaries WHERE dictionary_id = :dictionaryId")
    fun getDictionaryById(dictionaryId: Long): Observable<DictionaryEntry>
}