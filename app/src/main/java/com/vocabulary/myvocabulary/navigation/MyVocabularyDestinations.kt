package com.vocabulary.myvocabulary.navigation

import kotlinx.serialization.Serializable

/**
 * Contract for information needed on every MyVocabulary navigation destination
 */
interface MyVocabularyDestinations

/**
 * MyVocabulary app navigation destinations
 */
@Serializable
object Home : MyVocabularyDestinations

@Serializable
object DictionaryList : MyVocabularyDestinations

@Serializable
data class WordList(
    val dictionaryId: Long,
    val dictionaryName: String
) : MyVocabularyDestinations

@Serializable
object About : MyVocabularyDestinations

@Serializable
object QuizList : MyVocabularyDestinations