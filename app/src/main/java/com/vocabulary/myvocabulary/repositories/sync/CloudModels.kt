package com.vocabulary.myvocabulary.repositories.sync

import com.google.firebase.firestore.IgnoreExtraProperties
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.words.Word
import java.util.Date

@IgnoreExtraProperties
data class CloudDictionary(
    var id: Long = 0,
    var name: String = "",
    var created: Long = 0,
    var lastPracticed: Long? = null,
    var lastResult: Int? = null,
    var finishedCount: Int = 0,
    var totalScore: Int = 0
)

@IgnoreExtraProperties
data class CloudWord(
    var id: Long = 0,
    var dictionaryId: Long = 0,
    var word: String = "",
    var translation: String = "",
    var beenAsked: Int = 0,
    var failed: Int = 0,
    var passed: Int = 0,
    var created: Long = 0,
    var lastResult: Boolean = false
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
