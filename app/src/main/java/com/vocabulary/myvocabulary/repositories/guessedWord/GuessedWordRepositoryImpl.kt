package com.vocabulary.myvocabulary.repositories.guessedWord

import com.vocabulary.myvocabulary.ui.quizzes.GuessedWord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GuessedWordRepositoryImpl : GuessedWordRepository {
    private var guessMap: MutableMap<Long, String> = mutableMapOf()
    private val _guessedWordMap = MutableStateFlow<GuessedMapData>(GuessedMapData.EMPTY)
    override val guessedWordMap: Flow<GuessedMapData> = _guessedWordMap

    override fun addToGuessedWordMap(lastGuess: GuessedWord) {
        guessMap[lastGuess.wordId] = lastGuess.guess.trim()
        _guessedWordMap.value = GuessedMapData.GuessedData(guessMap)
    }

    override fun resetGuessedWordMap() {
        guessMap = mutableMapOf()
        _guessedWordMap.value = GuessedMapData.EMPTY
    }

}