package com.vocabulary.myvocabulary.repositories.guessedWord

import com.vocabulary.myvocabulary.ui.quizzes.GuessedWord
import kotlinx.coroutines.flow.Flow

interface GuessedWordRepository {
    val guessedWordMap: Flow<GuessedMapData>
    fun addToGuessedWordMap(lastGuess: GuessedWord)
    fun resetGuessedWordMap()
}