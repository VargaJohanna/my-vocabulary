package com.vocabulary.myvocabulary.repositories.dictionary

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface DictionaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDictionary(dictionaryEntry: DictionaryEntry): Long

    @Update
    suspend fun updateDictionary(dictionaryEntry: DictionaryEntry)

    @Delete
    suspend fun deleteDictionary(dictionaryEntry: DictionaryEntry)

    @Query("SELECT * FROM dictionaries")
    fun getAllDictionaries(): Flow<List<DictionaryEntry>>

    @Query("SELECT count(*) FROM dictionaries")
    fun getNumberOfDictionaries(): Flow<Int>

    @Query("UPDATE dictionaries SET dictionary_last_practiced = :date WHERE dictionary_id = :id")
    suspend fun updateLastPracticed(id: Long, date: Date)

    @Query("UPDATE dictionaries SET dictionary_last_practiced = :date, dictionary_last_result = :result, dictionary_finished_count = dictionary_finished_count + 1, dictionary_total_score = dictionary_total_score + :result WHERE dictionary_id = :id")
    suspend fun updateDictionaryStats(id: Long, date: Date, result: Int)
}