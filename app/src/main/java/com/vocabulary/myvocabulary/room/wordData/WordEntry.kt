package com.vocabulary.myvocabulary.room.wordData

import androidx.room.*
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryEntry
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.utils.DateTypeConverter
import java.util.*

@Entity(tableName = "words",
        foreignKeys = [ForeignKey(entity = DictionaryEntry::class,
                parentColumns = arrayOf("dictionary_id"),
                childColumns = arrayOf("container_dictionary_id"),
                onDelete = ForeignKey.CASCADE)],
        indices = [Index("container_dictionary_id")])
data class WordEntry(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "word_id") val wordId: Long,
        @ColumnInfo(name = "container_dictionary_id") val containerDictionaryId: Long,
        val word: String,
        val translation: String,
        @ColumnInfo(name = "been_asked") val beenAsked: Int,
        val failed: Int,
        val passed: Int,
        @ColumnInfo(name = "created") @TypeConverters(DateTypeConverter::class) val created: Date,
        @ColumnInfo(name = "last_result") val lastResult: Boolean
) {
    @Ignore
    constructor(
            containerDictionaryId: Long,
                word: String,
                translation: String,
                beenAsked: Int,
                failed: Int,
                passed: Int,
                created: Date,
                lastResult: Boolean
    ) : this(0, containerDictionaryId, word, translation, beenAsked, failed, passed, created, lastResult)
}

fun WordEntry.toWord() = Word(wordId, containerDictionaryId, word, translation, beenAsked, failed, passed, created, lastResult)