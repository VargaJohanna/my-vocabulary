package com.vocabulary.myvocabulary.ui.results

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.vocabulary.myvocabulary.TestScheduler
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.quizzes.GuessedWord
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Single
import org.junit.Assert.assertEquals
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
    private var wordToUpdateCorrect = Word(
            wordId = 2L,
            containerDictionaryId = 2L,
            word = "a",
            translation = "a",
            created = Date(2019, 4, 20, 2, 2, 2),
            beenAsked = 1,
            failed = 0,
            passed = 1
    )
    private val guessList = asList(
            GuessedWord(2L, "a")
    )

    @Test
    fun `should update guessedWordMap when latestGuess() is called`() {
        val resultViewModel = givenResultViewModel()

        resultViewModel.latestGuess(guessToTest1)

        assertEquals(guessToTest1.guess, resultViewModel.getGuessedWordMap()[guessToTest1.wordId])
    }

    @Test
    fun `should delegate to wordRepository dictionaryId when getGuessResult() is called`() {
        val resultViewModel = givenResultViewModelWithData()
        resultViewModel.setGuessedWordMap(guessList)

        resultViewModel.getGuessResult()

        verify(wordRepository).getWordById(2L)
    }

    @Test
    fun `should update word when getGuessResult() is called and last guess is correct`() {
        val resultViewModel = givenResultViewModelWithData()
        resultViewModel.setGuessedWordMap(guessList)
        val updatedWord = Word(
                wordId = 2L,
                containerDictionaryId = 2L,
                word = "a",
                translation = "a",
                created = Date(2019, 4, 20, 2, 2, 2),
                beenAsked = 2,
                failed = 0,
                passed = 2,
                lastResult = true,
                lastGuess = "a"
        )

        resultViewModel.getGuessResult()

        verify(wordRepository).updateWord(updatedWord)
    }

    @Test
    fun `should update word when getGuessResult() is called and last guess is wrong`() {
        wordToUpdateCorrect = Word(
                wordId = 2L,
                containerDictionaryId = 2L,
                word = "q",
                translation = "q",
                created = Date(2019, 4, 20, 2, 2, 2),
                beenAsked = 1,
                failed = 0,
                passed = 1
        )
        val resultViewModel = givenResultViewModelWithData()
        resultViewModel.setGuessedWordMap(guessList)
        val updatedWord = Word(
                wordId = 2L,
                containerDictionaryId = 2L,
                word = "q",
                translation = "q",
                created = Date(2019, 4, 20, 2, 2, 2),
                beenAsked = 2,
                failed = 1,
                passed = 1,
                lastResult = false,
                lastGuess = "a"
        )

        resultViewModel.getGuessResult()

        verify(wordRepository).updateWord(updatedWord)
    }

    @Test
    fun `should update quiz list when getGuessResult() is called`() {
        val resultViewModel = givenResultViewModelWithData()
        resultViewModel.setGuessedWordMap(guessList)
        val updatedWord = Word(
                wordId = 2L,
                containerDictionaryId = 2L,
                word = "a",
                translation = "a",
                created = Date(2019, 4, 20, 2, 2, 2),
                beenAsked = 2,
                failed = 0,
                passed = 2,
                lastResult = true,
                lastGuess = "a"
        )

        resultViewModel.getGuessResult()

        verify(quizRepository).updateQuizList(asList(updatedWord))
    }

    @Test
    fun `should update liveGuessedWordList when getGuessResult() is called`() {
        val resultViewModel = givenResultViewModelWithData()
        resultViewModel.setGuessedWordMap(guessList)
        val updatedWord = Word(
                wordId = 2L,
                containerDictionaryId = 2L,
                word = "a",
                translation = "a",
                created = Date(2019, 4, 20, 2, 2, 2),
                beenAsked = 2,
                failed = 0,
                passed = 2,
                lastResult = true,
                lastGuess = "a"
        )

        resultViewModel.getGuessResult()
        resultViewModel.getLiveGuessedList().observeForever(mock())

        assertEquals(asList(updatedWord), resultViewModel.getLiveGuessedList().value)
    }

    @Test
    fun `should clear all guess collections when resetGuessedWordCollections() is called`() {
        val resultViewModel = givenResultViewModelWithData()
        resultViewModel.setGuessedWordMap(guessList)

        resultViewModel.resetGuessedWordCollections()
        resultViewModel.getLiveGuessedList().observeForever(mock())

        assertEquals(emptyList<Word>(), resultViewModel.getLiveGuessedList().value)
        assertEquals(emptyMap<Long, String>(), resultViewModel.getGuessedWordMap())
    }

    @Test
    fun `should start new quick quiz when startNew() is called`() {
        val resultViewModel = givenResultViewModel()

        resultViewModel.startNew(dictionaryId, QuizTypes.QuickQuiz)

        verify(quizRepository).resetQuizList(dictionaryId, QuizTypes.QuickQuiz)
    }

    @Test
    fun `should start new full quiz when startNew() is called`() {
        val resultViewModel = givenResultViewModel()

        resultViewModel.startNew(dictionaryId, QuizTypes.FullQuiz)

        verify(quizRepository).resetQuizList(dictionaryId, QuizTypes.FullQuiz)
    }

    @Test
    fun `should start new weaknesses quiz when startNew() is called`() {
        val resultViewModel = givenResultViewModel()

        resultViewModel.startNew(dictionaryId, QuizTypes.WeakestQuiz)

        verify(quizRepository).resetQuizList(dictionaryId, QuizTypes.WeakestQuiz)
    }

    private fun givenResultViewModel(): ResultViewModel {
        whenever(wordRepository.getWordById(2L)).thenReturn(Single.never())
        return ResultViewModel(dictionaryId, wordRepository, TestScheduler(), quizRepository)
    }

    private fun givenResultViewModelWithData(): ResultViewModel {
        whenever(wordRepository.getWordById(2L)).thenReturn(Single.just(wordToUpdateCorrect))
        return ResultViewModel(dictionaryId, wordRepository, TestScheduler(), quizRepository)
    }
}