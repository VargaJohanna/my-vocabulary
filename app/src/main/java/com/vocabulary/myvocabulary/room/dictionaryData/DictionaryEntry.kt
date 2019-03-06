package com.vocabulary.myvocabulary.room.dictionaryData

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.utils.DateTypeConverter
import java.util.*

@Entity(tableName = "dictionaries")
data class DictionaryEntry(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "dictionary_id") val dictionaryId: Long?,
        @ColumnInfo(name = "dictionary_name") val dictionaryName: String?,
        @ColumnInfo(name = "dictionary_created") @TypeConverters(DateTypeConverter::class) val dictionaryCreated: Date?
)

fun DictionaryEntry.toDictionary() = Dictionary(dictionaryId, dictionaryName, dictionaryCreated)