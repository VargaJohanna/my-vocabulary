package com.vocabulary.myvocabulary.ui.results

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.vocabulary.myvocabulary.domain.ProcessQuizResultsUseCase
import com.vocabulary.myvocabulary.domain.QuizResult
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedMapData
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedWordRepository
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.testing.MainCoroutineRule
import com.vocabulary.myvocabulary.ui.quizzes.GuessedWord
import com.vocabulary.myvocabulary.ui.quizzes.QuizDirectionType
import com.vocabulary.myvocabulary.ui.quizzes.toInt
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()
    private val processQuizResultsUseCase: ProcessQuizResultsUseCase = mockk()
    private val quizRepository: QuizRepository = mockk(relaxed = true)
    private val guessedWordRepository: GuessedWordRepository = mockk()
    
    private lateinit var viewModel: ResultViewModel
    private val dictionaryId = 1L
    private val guessedWordMapFlow = MutableStateFlow<GuessedMapData>(GuessedMapData.EMPTY)

    @Before
    fun setup() {
        every { guessedWordRepository.guessedWordMap } returns guessedWordMapFlow

        viewModel = ResultViewModel(
            dictionaryId = dictionaryId,
            quizDirection = QuizDirectionType.AskWord.toInt(),
            quizRepository = quizRepository,
            guessedWordRepository = guessedWordRepository,
            processQuizResultsUseCase = processQuizResultsUseCase,
        )
    }

    @Test
    fun `fetchResults should emit Data state when processing is successful`() = runTest {
        // Arrange
        val map = mapOf(1L to "guess")
        guessedWordMapFlow.value = GuessedMapData.GuessedData(map)
        
        val quizResult = QuizResult(
            processedWords = emptyList(),
            percentage = 100,
            numberOfPassed = 1,
            allPassed = true
        )
        coEvery { processQuizResultsUseCase(dictionaryId, map, any()) } returns quizResult

        // Act & Assert
        viewModel.resultUiState.test {
            assertThat(awaitItem()).isEqualTo(ResultUiState.Loading)
            
            viewModel.fetchResults()
            
            val result = awaitItem()
            assertThat(result is ResultUiState.Data).isTrue()
            val data = result as ResultUiState.Data
            assertThat(data.percentage).isEqualTo(100)
            assertThat(data.allPassed).isTrue()
        }
    }

    @Test
    fun `fetchResults should emit Error state when guessed map is empty`() = runTest {
        // Arrange
        guessedWordMapFlow.value = GuessedMapData.EMPTY

        // Act & Assert
        viewModel.resultUiState.test {
            assertThat(awaitItem()).isEqualTo(ResultUiState.Loading)
            
            viewModel.fetchResults()
            
            val result = awaitItem()
            assertThat(result is ResultUiState.Error).isTrue()
        }
    }

    @Test
    fun `fetchResults should emit Error state when use case fails`() = runTest {
        // Arrange
        val map = mapOf(1L to "guess")
        guessedWordMapFlow.value = GuessedMapData.GuessedData(map)
        coEvery { processQuizResultsUseCase(any(), any(), any()) } throws Exception("Test Error")

        // Act & Assert
        viewModel.resultUiState.test {
            assertThat(awaitItem()).isEqualTo(ResultUiState.Loading)
            
            viewModel.fetchResults()
            
            val result = awaitItem()
            assertThat(result is ResultUiState.Error).isTrue()
            assertThat((result as ResultUiState.Error).message).isEqualTo("Failed to process results. Error: Test Error")
        }
    }

    @Test
    fun `resetGuessedWordCollections should reset state and repository`() = runTest {
        // Arrange
        every { guessedWordRepository.resetGuessedWordMap() } just runs

        // Act
        viewModel.resetGuessedWordCollections()

        // Assert
        verify { guessedWordRepository.resetGuessedWordMap() }
        assertThat(viewModel.resultUiState.value).isEqualTo(ResultUiState.Loading)
    }

    @Test
    fun `latestGuess should delegate to repository`() {
        // Arrange
        val guess = GuessedWord(1L, "guess")
        every { guessedWordRepository.addToGuessedWordMap(guess) } just runs

        // Act
        viewModel.latestGuess(guess)

        // Assert
        verify { guessedWordRepository.addToGuessedWordMap(guess) }
    }
}
