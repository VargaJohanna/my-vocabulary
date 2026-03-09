package com.vocabulary.myvocabulary.ui.words

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import io.mockk.*
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class WordDetailsViewModelTest {

    // Rule to handle LiveData/Architecture components execution
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Mock dependencies
    private val wordRepository: WordRepository = mockk()
    private val rxSchedulers: RxSchedulers = mockk()

    private lateinit var viewModel: WordDetailsViewModel

    // Sample data for testing
    private val testId = 1L
    private val testWord = Word(
        wordId = testId,
        containerDictionaryId = 10,
        word = "Apple",
        translation = "Alma",
        beenAsked = 0,
        failed = 0,
        passed = 0,
        lastResult = false,
        created = Date(),
        lastGuess = ""
    )

    @Before
    fun setup() {
        // Mock Schedulers to run everything immediately on the same thread
        every { rxSchedulers.io() } returns Schedulers.trampoline()
        every { rxSchedulers.main() } returns Schedulers.trampoline()

        viewModel = WordDetailsViewModel(wordRepository, rxSchedulers)
    }

    @Test
    fun `fetchWordById should update currentWord state when repository returns data`() = runTest {
        // Arrange
        every { wordRepository.getWordById(testId) } returns Single.just(testWord)

        // Act
        viewModel.fetchWordById(testId)

        // Assert
        verify { wordRepository.getWordById(testId) }
        assertEquals(testWord, viewModel.currentWord.value)
    }
}