package com.vocabulary.myvocabulary.ui.results

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.vocabulary.myvocabulary.TestScheduler
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.quizzes.GuessedWord
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.*
import java.util.Arrays.asList

class ResultViewModelTest {
    @Rule
    @JvmField
    var mockito = InstantTaskExecutorRule()

    private val dictionaryId = 2L
    private val wordRepository = mock<WordRepository>()
    private val quizRepository = mock<QuizRepository>()
    private val guessToTest1 = GuessedWord(1L, "b")
    private val wordToUpdate = Word(wordId = 3L,
            containerDictionaryId = 2L,
            word = "b",
            translation = "b",
            created = Date(2019, 4, 20, 2, 2, 2),
            beenAsked=1,
            failed=0,
            passed=1)
    private val guessList = asList(
            GuessedWord(2L, "a"),
            GuessedWord(3L, "b"),
            GuessedWord(4L, "e")
    )


    @Test
    fun `should update guessedWordMap when latestGuess() is called`() {
        val resultViewModel = givenResultViewModel()

        resultViewModel.latestGuess(guessToTest1)

        assertEquals(guessToTest1.guess, resultViewModel.getGuessedWordMap()[guessToTest1.wordId])
    }

    @Test
    fun `should delegate to wordRepository dictionaryId when getGuessResult() is called`() {
        val resultViewModel = givenResultViewModel()
        resultViewModel.setGuessedWordMap(guessList)

        resultViewModel.getGuessResult()

        verify(wordRepository).getWordById(2L)
    }

//    @Test
//    fun `should update word when getGuessResult() is called`() {
//        val resultViewModel = givenResultViewModelWithData()
//        resultViewModel.setGuessedWordMap(guessList)
//
//        resultViewModel.getGuessResult()
//
//        verify(wordRepository).updateWord(wordToUpdate)
//    }

    private fun givenResultViewModel(): ResultViewModel {
        whenever(wordRepository.getWordById(2L)).thenReturn(Single.never())
        return ResultViewModel(dictionaryId, wordRepository, TestScheduler(), quizRepository)
    }

    private fun givenResultViewModelWithData(): ResultViewModel {
        whenever(wordRepository.getWordById(2L)).thenReturn(Single.just(wordToUpdate))
        return ResultViewModel(dictionaryId, wordRepository, TestScheduler(), quizRepository)
    }

}