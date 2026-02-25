package com.vocabulary.myvocabulary.repositories.dictionary

import androidx.room.*
import io.reactivex.Observable
import java.util.Date

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

    @Query("UPDATE dictionaries SET dictionary_last_practiced = :date WHERE dictionary_id = :id")
    fun updateLastPracticed(id: Long, date: Date)

    @Query("UPDATE dictionaries SET dictionary_last_practiced = :date, dictionary_last_result = :result, dictionary_finished_count = dictionary_finished_count + 1 WHERE dictionary_id = :id")
    fun updateDictionaryStats(id: Long, date: Date, result: Int)
}