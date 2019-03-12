package com.vocabulary.myvocabulary.ui.dictionaries

import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryEntry
import java.util.*

data class Dictionary(
        val dictionaryId: Long = 0,
        val dictionaryName: String,
        val dictionaryCreated: Date)

fun Dictionary.toDictionaryEntry(): DictionaryEntry {
    return if (dictionaryId == 0L) {
        DictionaryEntry(dictionaryName, dictionaryCreated)
    } else {
        DictionaryEntry(dictionaryId, dictionaryName, dictionaryCreated)
    }
}