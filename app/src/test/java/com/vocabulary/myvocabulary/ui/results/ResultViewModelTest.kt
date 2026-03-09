package com.vocabulary.myvocabulary.ui.results

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.input.key.type
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedMapData
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedWordRepository
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.QuizDirectionType
import com.vocabulary.myvocabulary.ui.quizzes.toInt
import com.vocabulary.myvocabulary.ui.words.Word
import io.mockk.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class ResultViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val wordRepository: WordRepository = mockk()
    private val dictionaryRepository: DictionaryRepository = mockk(relaxed = true)
    private val rxSchedulers: RxSchedulers = mockk()
    private val quizRepository: QuizRepository = mockk(relaxed = true)
    private val guessedWordRepository: GuessedWordRepository = mockk(relaxed = true)

    private lateinit var viewModel: ResultViewModel
    private val dictionaryId = 1L
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Schedulers for synchronous execution
        every { rxSchedulers.io() } returns Schedulers.trampoline()
        every { rxSchedulers.main() } returns Schedulers.trampoline()

        viewModel = ResultViewModel(
            dictionaryId = dictionaryId,
            quizDirection = QuizDirectionType.AskWord.toInt(), // Assuming 0 or 1
            wordRepository = wordRepository,
            dictionaryRepository = dictionaryRepository,
            rxSchedulers = rxSchedulers,
            quizRepository = quizRepository,
            guessedWordRepository = guessedWordRepository
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observeGuessedWordMap should correctly evaluate results and update state`() {
        // Arrange
        val wordId = 10L
        val userGuess = "Alma"
        val correctWord = Word(wordId, 1, "Apple", "Alma", created = Date())

        val guessedData = mapOf(wordId to userGuess)
        every { guessedWordRepository.guessedWordMap } returns Observable.just(
            GuessedMapData.GuessedData(
                guessedData
            )
        )
        every { wordRepository.getWordById(wordId) } returns Single.just(correctWord)
        every { wordRepository.updateWord(any()) } returns Unit

        // Act
        viewModel.observeGuessedWordMap()

        // Assert
        // verify percentage calculation: 1 word, 1 correct = 100%
        assertEquals(100, viewModel.getResultPercentage().value)
        assertEquals(1, viewModel.getNumOfPassed().value)

        // Verify repository calls
        verify { dictionaryRepository.saveQuizStats(dictionaryId, 100) }
        verify { dictionaryRepository.onQuizFinished(dictionaryId) }
        verify { wordRepository.updateWord(match { it.lastResult == true && it.lastGuess == userGuess }) }
    }

    @Test
    fun `observeGuessedWordMap should set isAllPassed to false on wrong answer`() {
        // Arrange
        val wordId = 20L
        val userGuess = "WrongAnswer"
        val correctWord = Word(wordId, 1, "Bread", "Kenyér", created = Date())

        every { guessedWordRepository.guessedWordMap } returns Observable.just(
            GuessedMapData.GuessedData(mapOf(wordId to userGuess))
        )
        every { wordRepository.getWordById(wordId) } returns Single.just(correctWord)
        every { wordRepository.updateWord(any()) } returns Unit

        // Act
        viewModel.observeGuessedWordMap()

        // Assert
        assertEquals(0, viewModel.getResultPercentage().value)
        assertEquals(false, viewModel.isAllPassed)
    }

    @Test
    fun `resetGuessedWordCollections should clear data and reset isAllPassed`() {
        // Arrange
        viewModel.isAllPassed = false

        // Act
        viewModel.resetGuessedWordCollections()

        // Assert
        verify { guessedWordRepository.resetGuessedWordMap() }
        assertEquals(true, viewModel.isAllPassed)
        assertEquals(0, viewModel.getGuessedList().value.size)
    }

    @Test
    fun `saveLastPracticeOfDictionary should delegate to dictionaryRepository`() {
        // Act
        viewModel.saveLastPracticeOfDictionary(dictionaryId)

        // Assert
        verify { dictionaryRepository.onQuizFinished(dictionaryId) }
    }
}