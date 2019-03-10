package com.vocabulary.myvocabulary.room.dictionaryData

import androidx.room.*
import io.reactivex.Single

@Dao
interface DictionaryDao {
    @Insert
    fun insertDictionary(dictionaryEntry: DictionaryEntry): Long

    @Update
    fun updateDictionary(dictionaryEntry: DictionaryEntry)

    @Delete
    fun deleteDictionary(dictionaryEntry: DictionaryEntry)

    @Query("SELECT * FROM dictionaries")
    fun getAllDictionaries(): Single<List<DictionaryEntry>>

    @Query("SELECT * FROM dictionaries WHERE dictionary_id = :dictionaryId")
    fun getDictionaryById(dictionaryId: Long): Single<DictionaryEntry>

    @Query("SELECT count(*) FROM dictionaries")
    fun getNumberOfDictionaries(): Single<Int>
}