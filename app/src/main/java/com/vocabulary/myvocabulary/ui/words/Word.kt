package com.vocabulary.myvocabulary.ui.words

import com.vocabulary.myvocabulary.room.wordData.WordEntry
import java.util.*

data class Word (
        val wordId: Long,
        val containerDictionaryId: Long,
        val word: String,
        val translation: String,
        val beenAsked: Int,
        val failed: Int,
        val passed: Int,
        val created: Date
)

fun Word.toWordEntry() = WordEntry(wordId, containerDictionaryId, word, translation, beenAsked, failed, passed, created)