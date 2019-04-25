package com.vocabulary.myvocabulary.repositories.guessedWord

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.vocabulary.myvocabulary.ui.quizzes.GuessedWord
import org.junit.Rule
import org.junit.Test

class GuessedWordRepositoryImplTest {
    @Rule
    @JvmField
    var mockito = InstantTaskExecutorRule()

    @Test
    fun `should update guessedWordMap when addToGuessedWordMap() is called`() {
        val guessedWordRepository = givenGuessedWordRepository()
        val lastGuess = GuessedWord(2L, "a")

        guessedWordRepository.addToGuessedWordMap(lastGuess)
        val testObserver = guessedWordRepository.guessedWordMap.test()

        testObserver.assertValue(GuessedMapData.GuessedData(mapOf(lastGuess.wordId to lastGuess.guess)))
                .assertNoErrors()
                .dispose()
    }

    @Test
    fun `should reset guessedWordMap when resetGuessedWordMap() is called`() {
        val guessedWordRepository = givenGuessedWordRepository()

        guessedWordRepository.resetGuessedWordMap()
        val testObserver = guessedWordRepository.guessedWordMap.test()

        testObserver.assertValue(GuessedMapData.EMPTY)
                .assertNoErrors()
                .dispose()
    }

    private fun givenGuessedWordRepository(): GuessedWordRepository {
        return GuessedWordRepositoryImpl()
    }
}