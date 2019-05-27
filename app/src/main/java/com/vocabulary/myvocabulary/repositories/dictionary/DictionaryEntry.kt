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
        @ColumnInfo(name = "dictionary_created") @TypeConverters(DateTypeConverter::class) var dictionaryCreated: Date)
{
    @Ignore
    constructor(dictionaryName: String, dictionaryCreated: Date) : this(0, dictionaryName, dictionaryCreated)
}

fun DictionaryEntry.toDictionary() = Dictionary(dictionaryId, dictionaryName, dictionaryCreated)