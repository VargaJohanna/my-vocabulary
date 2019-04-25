package com.vocabulary.myvocabulary.repositories.guessedWord

import com.vocabulary.myvocabulary.ui.quizzes.GuessedWord
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * To store data in memory while the app is running.
 */
class GuessedWordRepositoryImpl : GuessedWordRepository {
    private var guessMap: MutableMap<Long, String> = mutableMapOf()
    private val _guessedWordMap: BehaviorSubject<GuessedMapData> = BehaviorSubject.create<GuessedMapData>()
    override val guessedWordMap: Observable<GuessedMapData> = _guessedWordMap

    override fun addToGuessedWordMap(lastGuess: GuessedWord) {
        guessMap[lastGuess.wordId] = lastGuess.guess.trim()
        _guessedWordMap.onNext(GuessedMapData.GuessedData(guessMap))
    }

    override fun resetGuessedWordMap() {
        guessMap = mutableMapOf()
        _guessedWordMap.onNext(GuessedMapData.EMPTY)
    }

}