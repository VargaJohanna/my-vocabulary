package com.vocabulary.myvocabulary.repositories.sync

import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.words.Word
import java.util.Date

data class CloudDictionary(
    val id: Long = 0,
    val name: String = "",
    val created: Long = 0,
    val lastPracticed: Long? = null,
    val lastResult: Int? = null,
    val finishedCount: Int = 0,
    val totalScore: Int = 0
)

data class CloudWord(
    val id: Long = 0,
    val dictionaryId: Long = 0,
    val word: String = "",
    val translation: String = "",
    val beenAsked: Int = 0,
    val failed: Int = 0,
    val passed: Int = 0,
    val created: Long = 0,
    val lastResult: Boolean = false
)

fun CloudDictionary.toLocal(): Dictionary = Dictionary(
    dictionaryId = id,
    dictionaryName = name,
    dictionaryCreated = Date(created),
    dictionaryLastPracticed = lastPracticed?.let { Date(it) },
    dictionaryLastResult = lastResult,
    dictionaryFinishedCount = finishedCount,
    dictionaryTotalScore = totalScore
)

fun CloudWord.toLocal(): Word = Word(
    wordId = id,
    containerDictionaryId = dictionaryId,
    word = word,
    translation = translation,
    beenAsked = beenAsked,
    failed = failed,
    passed = passed,
    created = Date(created),
    lastResult = lastResult
)

fun Dictionary.toCloud(): CloudDictionary = CloudDictionary(
    id = dictionaryId,
    name = dictionaryName,
    created = dictionaryCreated.time,
    lastPracticed = dictionaryLastPracticed?.time,
    lastResult = dictionaryLastResult,
    finishedCount = dictionaryFinishedCount,
    totalScore = dictionaryTotalScore
)

fun Word.toCloud(): CloudWord = CloudWord(
    id = wordId,
    dictionaryId = containerDictionaryId,
    word = word,
    translation = translation,
    beenAsked = beenAsked,
    failed = failed,
    passed = passed,
    created = created.time,
    lastResult = lastResult
)
