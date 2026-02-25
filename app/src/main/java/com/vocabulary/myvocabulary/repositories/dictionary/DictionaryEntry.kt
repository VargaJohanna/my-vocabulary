package com.vocabulary.myvocabulary.repositories.dictionary

import androidx.room.*
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.utils.DateTypeConverter
import java.util.*

@Entity(tableName = "dictionaries")
data class DictionaryEntry(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "dictionary_id") var dictionaryId: Long,
        @ColumnInfo(name = "dictionary_name") var dictionaryName: String,
        @ColumnInfo(name = "dictionary_created") @TypeConverters(DateTypeConverter::class) var dictionaryCreated: Date,
        @ColumnInfo(name = "dictionary_last_practiced") @TypeConverters(DateTypeConverter::class) var dictionaryLastPracticed: Date? = null,
        @ColumnInfo(name = "dictionary_last_result") var dictionaryLastResult: Int? = null,
        @ColumnInfo(name = "dictionary_finished_count") var dictionaryFinishedCount: Int = 0
)
{
    @Ignore
    constructor(dictionaryName: String, dictionaryCreated: Date) : this(0, dictionaryName, dictionaryCreated)
}

fun DictionaryEntry.toDictionary() = Dictionary(dictionaryId, dictionaryName, dictionaryCreated, dictionaryLastPracticed, dictionaryLastResult, dictionaryFinishedCount)