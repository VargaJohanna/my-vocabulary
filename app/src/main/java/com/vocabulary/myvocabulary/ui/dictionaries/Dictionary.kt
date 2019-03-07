package com.vocabulary.myvocabulary.ui.dictionaries

import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryEntry
import java.util.*

data class Dictionary (
        val dictionaryId: Long,
        val dictionaryName: String,
        val dictionaryCreated: Date
)

fun Dictionary.toDictionaryEntry() = DictionaryEntry(dictionaryId, dictionaryName, dictionaryCreated)