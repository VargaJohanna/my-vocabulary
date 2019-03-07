package com.vocabulary.myvocabulary.room.dictionaryData

import androidx.room.*
import io.reactivex.Single

@Dao
interface DictionaryDao {
    @Insert
    fun insertDictionary(dictionary: DictionaryEntry)

    @Update
    fun updateDictionary(dictionary: DictionaryEntry)

    @Delete
    fun deleteDictionary(dictionary: DictionaryEntry)

    @Query("SELECT * FROM dictionaries")
    fun getAllDictionaries(): Single<List<DictionaryEntry>>

    @Query("SELECT * FROM dictionaries WHERE dictionary_id = :dictionaryId")
    fun getDictionaryById(dictionaryId: Long): Single<DictionaryEntry>

    @Query("SELECT count(*) FROM dictionaries")
    fun getNumberOfDictionaries(): Single<Int>
}