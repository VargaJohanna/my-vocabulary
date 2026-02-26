package com.vocabulary.myvocabulary.ui.dictionaries

import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryEntry
import java.util.*

data class Dictionary(
        val dictionaryId: Long = 0,
        val dictionaryName: String,
        val dictionaryCreated: Date,
        val dictionaryLastPracticed: Date?,
        val dictionaryLastResult: Int?,
        val dictionaryFinishedCount: Int,
        val dictionaryTotalScore: Int
) {
    val averageResult: Float
        get() = if (dictionaryFinishedCount > 0) {
            dictionaryTotalScore.toFloat() / dictionaryFinishedCount
        } else 0f
}

fun Dictionary.toDictionaryEntry(): DictionaryEntry {
    return if (dictionaryId == 0L) {
        DictionaryEntry(dictionaryName, dictionaryCreated, )
    } else {
        DictionaryEntry(dictionaryId, dictionaryName, dictionaryCreated, dictionaryLastPracticed, dictionaryLastResult, dictionaryFinishedCount, dictionaryTotalScore)
    }
}