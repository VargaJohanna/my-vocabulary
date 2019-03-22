package com.vocabulary.myvocabulary.ui.words

import com.vocabulary.myvocabulary.room.wordData.WordEntry
import java.util.*

data class Word(
        val wordId: Long = 0,
        val containerDictionaryId: Long,
        val word: String,
        val translation: String,
        val beenAsked: Int = 0,
        val failed: Int = 0,
        val passed: Int = 0,
        val created: Date,
        val lastResult: Boolean = false,
        val lastGuess: String = ""
)

fun Word.toWordEntry(): WordEntry {
    return if (wordId == 0L) {
        WordEntry(containerDictionaryId, word, translation, beenAsked, failed, passed, created, lastResult, lastGuess)
    } else {
        WordEntry(wordId, containerDictionaryId, word, translation, beenAsked, failed, passed, created, lastResult, lastGuess)
    }
}