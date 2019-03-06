package com.vocabulary.myvocabulary.ui.dictionaries

import com.vocabulary.myvocabulary.data.dictionaryDatabase.DictionaryEntry
import java.util.*

data class Dictionary (
        val dictionaryId: Long? = null,
        val dictionaryName: String? = null,
        val dictionaryCreated: Date? = null
)

fun Dictionary.toDictionaryEntry() = DictionaryEntry(dictionaryId, dictionaryName, dictionaryCreated)