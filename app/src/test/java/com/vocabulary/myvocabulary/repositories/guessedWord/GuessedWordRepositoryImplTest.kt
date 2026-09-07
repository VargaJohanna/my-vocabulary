package com.vocabulary.myvocabulary.repositories.guessedWord

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.vocabulary.myvocabulary.ui.quizzes.GuessedWord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GuessedWordRepositoryImplTest {

    @Test
    fun `should update guessedWordMap when addToGuessedWordMap() is called`() = runTest {
        val guessedWordRepository = GuessedWordRepositoryImpl()
        val lastGuess = GuessedWord(2L, "a")

        guessedWordRepository.guessedWordMap.test {
            // Initial value
            assertThat(awaitItem()).isEqualTo(GuessedMapData.EMPTY)
            
            guessedWordRepository.addToGuessedWordMap(lastGuess)
            
            val result = awaitItem()
            assertThat(result).isEqualTo(GuessedMapData.GuessedData(mapOf(lastGuess.wordId to lastGuess.guess)))
        }
    }

    @Test
    fun `should reset guessedWordMap when resetGuessedWordMap() is called`() = runTest {
        val guessedWordRepository = GuessedWordRepositoryImpl()
        val lastGuess = GuessedWord(2L, "a")

        // First add something to reset later
        guessedWordRepository.addToGuessedWordMap(lastGuess)

        guessedWordRepository.guessedWordMap.test {
            // Initial value is the one we just added
            assertThat(awaitItem()).isEqualTo(GuessedMapData.GuessedData(mapOf(lastGuess.wordId to lastGuess.guess)))
            
            guessedWordRepository.resetGuessedWordMap()
            
            assertThat(awaitItem()).isEqualTo(GuessedMapData.EMPTY)
        }
    }
}
