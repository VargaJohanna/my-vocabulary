package com.vocabulary.myvocabulary.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.testing.MainCoroutineRule
import com.vocabulary.myvocabulary.testing.TestDispatchers
import com.vocabulary.myvocabulary.ui.words.Word
import io.mockk.*
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class ProcessQuizResultsUseCaseTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val wordRepository: WordRepository = mockk(relaxed = true)
    private val dictionaryRepository: DictionaryRepository = mockk(relaxed = true)
    private lateinit var processQuizResultsUseCase: ProcessQuizResultsUseCase
    private lateinit var testDispatcher: TestDispatchers

    private val dictionaryId = 100L
    private val testListOfWord = listOf(
        Word(1L, dictionaryId, "alma", "apple", 0, 0, 0, Date(12)),
        Word(2L, dictionaryId, "körte", "pear", 0, 0, 0, Date(12))
    )

    @Before
    fun setUp() {
        testDispatcher = TestDispatchers(mainCoroutineRule.testDispatcher)
        processQuizResultsUseCase = ProcessQuizResultsUseCase(wordRepository, dictionaryRepository, testDispatcher)
    }

    @Test
    fun `test processing when all words are correct`() = runTest {
        // Arrange: 0 is AskWord, answer should be translation
        every { wordRepository.getWordById(1L) } returns Single.just(testListOfWord[0])
        every { wordRepository.getWordById(2L) } returns Single.just(testListOfWord[1])

        val guesses = mapOf(1L to "apple", 2L to "pear")

        // Act
        val quizResult = processQuizResultsUseCase(dictionaryId, guesses, 0)

        // Assert
        assertThat(quizResult.percentage).isEqualTo(100)
        assertThat(quizResult.allPassed).isTrue()
        
        verify(exactly = 1) { dictionaryRepository.saveQuizStats(dictionaryId, 100) }
        verify(exactly = 1) { dictionaryRepository.onQuizFinished(dictionaryId) }
        verify(exactly = 2) { wordRepository.updateWord(any()) }
    }

    @Test
    fun `test processing when half of the words are correct`() = runTest {
        // Arrange
        every { wordRepository.getWordById(1L) } returns Single.just(testListOfWord[0])
        every { wordRepository.getWordById(2L) } returns Single.just(testListOfWord[1])

        val guesses = mapOf(1L to "wrong", 2L to "pear")

        // Act
        val quizResult = processQuizResultsUseCase(dictionaryId, guesses, 0)

        // Assert
        assertThat(quizResult.percentage).isEqualTo(50)
        assertThat(quizResult.allPassed).isFalse()

        verify(exactly = 1) { dictionaryRepository.saveQuizStats(dictionaryId, 50) }
    }

    @Test
    fun `test processing with AskTranslation direction`() = runTest {
        // Arrange: 1 is AskTranslation, answer should be word
        every { wordRepository.getWordById(1L) } returns Single.just(testListOfWord[0])
        every { wordRepository.getWordById(2L) } returns Single.just(testListOfWord[1])

        val guesses = mapOf(1L to "alma", 2L to "körte")

        // Act
        val quizResult = processQuizResultsUseCase(dictionaryId, guesses, 1)

        // Assert
        assertThat(quizResult.percentage).isEqualTo(100)
        assertThat(quizResult.allPassed).isTrue()
    }

    @Test
    fun `test processing ignores case and whitespace`() = runTest {
        // Arrange
        every { wordRepository.getWordById(1L) } returns Single.just(testListOfWord[0])

        val guesses = mapOf(1L to "  APPLE  ") // Mixed case and spaces

        // Act
        val quizResult = processQuizResultsUseCase(dictionaryId, guesses, 0)

        // Assert
        assertThat(quizResult.percentage).isEqualTo(100)
        assertThat(quizResult.allPassed).isTrue()
    }

    @Test
    fun `test processing with all incorrect answers`() = runTest {
        // Arrange
        every { wordRepository.getWordById(1L) } returns Single.just(testListOfWord[0])
        every { wordRepository.getWordById(2L) } returns Single.just(testListOfWord[1])

        val guesses = mapOf(1L to "wrong1", 2L to "wrong2")

        // Act
        val quizResult = processQuizResultsUseCase(dictionaryId, guesses, 0)

        // Assert
        assertThat(quizResult.percentage).isEqualTo(0)
        assertThat(quizResult.allPassed).isFalse()
        verify(exactly = 1) { dictionaryRepository.saveQuizStats(dictionaryId, 0) }
    }
}
