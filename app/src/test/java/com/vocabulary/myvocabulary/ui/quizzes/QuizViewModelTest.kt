package com.vocabulary.myvocabulary.ui.quizzes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.words.Word
import io.mockk.*
import io.reactivex.Completable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val rxSchedulers: RxSchedulers = mockk()
    private val quizRepository: QuizRepository = mockk()

    private lateinit var viewModel: QuizViewModel

    private val dictionaryId = 1L
    private val testDispatcher = UnconfinedTestDispatcher()

    // Use a BehaviorSubject to simulate the repository's quizList stream
    private val quizListSubject = BehaviorSubject.create<List<Word>>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Rx Schedulers to run immediately on the test thread
        every { rxSchedulers.io() } returns Schedulers.trampoline()
        every { rxSchedulers.main() } returns Schedulers.trampoline()

        // Mock the quizList observable from repository
        every { quizRepository.quizList } returns quizListSubject

        viewModel = QuizViewModel(
            dictionaryId = dictionaryId,
            isFailedOnly = false,
            rxSchedulers = rxSchedulers,
            quizRepository = quizRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `fetchQuizList should update quizList state when repository emits words`() {
        // Arrange
        val words = listOf(
            Word(1, dictionaryId, "Word1", "Trans1", created = Date()),
            Word(2, dictionaryId, "Word2", "Trans2", created = Date())
        )

        // Act
        viewModel.fetchQuizList()
        quizListSubject.onNext(words)

        // Assert
        assertEquals(2, viewModel.quizList.value.size)
        assertEquals(false, viewModel.isLoading.value)
        assertEquals(false, viewModel.isDictionaryEmpty)
    }

    @Test
    fun `observeQuizList should filter out blank words or translations`() {
        // Arrange
        val invalidWords = listOf(
            Word(1, dictionaryId, "", "Trans1", created = Date()), // Blank word
            Word(2, dictionaryId, "Word2", "", created = Date()),  // Blank translation
            Word(3, dictionaryId, "Valid", "Valid", created = Date())
        )

        // Act
        viewModel.fetchQuizList()
        quizListSubject.onNext(invalidWords)

        // Assert
        assertEquals(1, viewModel.quizList.value.size)
        assertEquals("Valid", viewModel.quizList.value[0].word)
    }

    @Test
    fun `observeQuizList should filter by failedOnly criteria when enabled`() {
        // Re-initialize ViewModel with failedOnly = true
        viewModel = QuizViewModel(dictionaryId, true, rxSchedulers, quizRepository)

        val mixedResults = listOf(
            Word(1, dictionaryId, "W1", "T1", created = Date(), lastResult = true), // Passed
            Word(2, dictionaryId, "W2", "T2", created = Date(), lastResult = false) // Failed
        )

        // Act
        viewModel.fetchQuizList()
        quizListSubject.onNext(mixedResults)

        // Assert
        assertEquals(1, viewModel.quizList.value.size)
        assertEquals("W2", viewModel.quizList.value[0].word)
    }

    @Test
    fun `startQuiz should call repository setQuizList when failedOnly is false`() = runTest {
        // Arrange
        val quizType = QuizTypes.FullQuiz
        every { quizRepository.setQuizList(dictionaryId, quizType) } returns Completable.complete()

        // Act
        viewModel.startQuiz(quizType, dictionaryId)

        // Assert
        verify { quizRepository.setQuizList(dictionaryId, quizType) }
    }

    @Test
    fun `startQuiz should NOT call repository setQuizList when failedOnly is true`() = runTest {
        // Re-initialize with failedOnly = true
        viewModel = QuizViewModel(dictionaryId, true, rxSchedulers, quizRepository)

        // Act
        viewModel.startQuiz(QuizTypes.FullQuiz, dictionaryId)

        // Assert
        verify(exactly = 0) { quizRepository.setQuizList(any(), any()) }
    }

    @Test
    fun `isDictionaryEmpty should be true when repository emits empty list`() {
        // Act
        viewModel.fetchQuizList()
        quizListSubject.onNext(emptyList())

        // Assert
        assertTrue(viewModel.isDictionaryEmpty)
        assertTrue(viewModel.quizList.value.isEmpty())
    }
}
