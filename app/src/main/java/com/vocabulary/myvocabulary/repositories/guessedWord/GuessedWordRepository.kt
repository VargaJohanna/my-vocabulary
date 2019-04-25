package com.vocabulary.myvocabulary.repositories.guessedWord

import com.vocabulary.myvocabulary.ui.quizzes.GuessedWord
import io.reactivex.Observable

interface GuessedWordRepository {
    val guessedWordMap: Observable<GuessedMapData>
    fun addToGuessedWordMap(lastGuess: GuessedWord)
    fun resetGuessedWordMap()
}