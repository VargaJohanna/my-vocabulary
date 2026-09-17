package com.vocabulary.myvocabulary.ui.words

import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.testing.MainCoroutineRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class WordDetailsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Mock dependencies
    private val wordRepository: WordRepository = mockk()

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
        viewModel = WordDetailsViewModel(wordRepository)
    }

    @Test
    fun `fetchWordById should update currentWord state when repository returns data`() = runTest {
        // Arrange
        coEvery { wordRepository.getWordById(testId) } returns testWord

        // Act
        viewModel.fetchWordById(testId)
        advanceUntilIdle()

        // Assert
        coVerify { wordRepository.getWordById(testId) }
        assertEquals(testWord, viewModel.currentWord.value)
    }
}
