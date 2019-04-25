package com.vocabulary.myvocabulary.ui.results

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.vocabulary.myvocabulary.TestScheduler
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedMapData
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedWordRepository
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.quizzes.GuessedWord
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import io.reactivex.Single
import junit.framework.Assert.assertEquals
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
    private val guessedWordRepository = mock<GuessedWordRepository>()
    private val guessToTest1 = GuessedWord(1L, "b")
    private var wordToUpdateCorrect = Word(
            wordId = 2L,
            containerDictionaryId = 2L,
            word = "a",
            translation = "a",
            created = Date(5),
            beenAsked = 1,
            failed = 0,
            passed = 1
    )
    private var guessedMap = mapOf(2L to "a")

    @Test
    fun `should delegate to guessedWordRepository when latestGuess() is called`() {
        val resultViewModel = givenResultViewModel()

        resultViewModel.latestGuess(guessToTest1)

        verify(guessedWordRepository).addToGuessedWordMap(guessToTest1)
    }

    @Test
    fun `should delegate to wordRepository dictionaryId when observeGuessedWordMap() is called`() {
        val resultViewModel = givenResultViewModelWithData()

        resultViewModel.observeGuessedWordMap()

        verify(wordRepository).getWordById(2L)
    }

    @Test
    fun `should update word when observeGuessedWordMap() is called and last guess is correct`() {
        val resultViewModel = givenResultViewModelWithData()
        val updatedWord = Word(
                wordId = 2L,
                containerDictionaryId = 2L,
                word = "a",
                translation = "a",
                created = Date(5),
                beenAsked = 2,
                failed = 0,
                passed = 2,
                lastResult = true,
                lastGuess = "a"
        )

        resultViewModel.observeGuessedWordMap()

        verify(wordRepository).updateWord(updatedWord)
    }

    @Test
    fun `should update word when observeGuessedWordMap() is called and last guess is wrong`() {
        wordToUpdateCorrect = Word(
                wordId = 2L,
                containerDictionaryId = 2L,
                word = "q",
                translation = "q",
                created = Date(5),
                beenAsked = 1,
                failed = 0,
                passed = 1
        )
        val updatedWord = Word(
                wordId = 2L,
                containerDictionaryId = 2L,
                word = "q",
                translation = "q",
                created = Date(5),
                beenAsked = 2,
                failed = 1,
                passed = 1,
                lastResult = false,
                lastGuess = "a"
        )
        val resultViewModel = givenResultViewModelWithData()

        resultViewModel.observeGuessedWordMap()

        verify(wordRepository).updateWord(updatedWord)
    }

    @Test
    fun `should update quiz list when observeGuessedWordMap() is called`() {
        val resultViewModel = givenResultViewModelWithData()
        val updatedWord = Word(
                wordId = 2L,
                containerDictionaryId = 2L,
                word = "a",
                translation = "a",
                created = Date(5),
                beenAsked = 2,
                failed = 0,
                passed = 2,
                lastResult = true,
                lastGuess = "a"
        )

        resultViewModel.observeGuessedWordMap()

        verify(quizRepository).updateQuizList(asList(updatedWord))
    }

    @Test
    fun `should update liveGuessedWordList when getGuessResult() is called`() {
        val resultViewModel = givenResultViewModelWithData()
        val updatedWord = Word(
                wordId = 2L,
                containerDictionaryId = 2L,
                word = "a",
                translation = "a",
                created = Date(5),
                beenAsked = 2,
                failed = 0,
                passed = 2,
                lastResult = true,
                lastGuess = "a"
        )

        resultViewModel.observeGuessedWordMap()
        resultViewModel.getLiveGuessedList().observeForever(mock())

        assertEquals(asList(updatedWord), resultViewModel.getLiveGuessedList().value)
    }

    @Test
    fun `should clear guess list when resetGuessedWordCollections() is called`() {
        val resultViewModel = givenResultViewModelWithData()

        resultViewModel.resetGuessedWordCollections()
        resultViewModel.getLiveGuessedList().observeForever(mock())

        assertEquals(emptyList<Word>(), resultViewModel.getLiveGuessedList().value)
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
        return ResultViewModel(dictionaryId, wordRepository, TestScheduler(), quizRepository, guessedWordRepository)
    }

    private fun givenResultViewModelWithData(): ResultViewModel {
        whenever(wordRepository.getWordById(2L)).thenReturn(Single.just(wordToUpdateCorrect))
        whenever(guessedWordRepository.guessedWordMap).thenReturn(Observable.just(GuessedMapData.GuessedData(guessedMap)))
        return ResultViewModel(dictionaryId, wordRepository, TestScheduler(), quizRepository, guessedWordRepository)
    }
}