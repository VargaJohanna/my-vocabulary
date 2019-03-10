package com.vocabulary.myvocabulary.ui.dictionaries

import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryEntry
import java.util.*

data class Dictionary (
        val dictionaryId: Long,
        val dictionaryName: String,
        val dictionaryCreated: Date) {
    constructor(dictionaryName: String, dictionaryCreated: Date) : this(0, dictionaryName, dictionaryCreated)
}

fun Dictionary.toDictionaryEntry() = DictionaryEntry(dictionaryId, dictionaryName, dictionaryCreated)