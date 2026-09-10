package com.vocabulary.myvocabulary.ui.quizzes

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.vocabulary.myvocabulary.repositories.quiz.CustomQuizRepository
import com.vocabulary.myvocabulary.testing.MainCoroutineRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizListViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val customQuizRepository: CustomQuizRepository = mockk(relaxed = true)
    private lateinit var viewModel: QuizListViewModel

    @Before
    fun setup() {
        viewModel = QuizListViewModel(customQuizRepository)
    }

    @Test
    fun `addCustomQuizSize should update repository when size is not null`() {
        // Arrange
        val size = 15

        // Act
        viewModel.addCustomQuizSize(size)

        // Assert
        verify { customQuizRepository.quizSize = size }
    }

    @Test
    fun `addCustomQuizSize should not update repository when size is null`() {
        // Act
        viewModel.addCustomQuizSize(null)

        // Assert
        verify(exactly = 0) { customQuizRepository.quizSize = any() }
    }

    @Test
    fun `setDialogState should update activeDialog flow`() = runTest {
        val dialog = QuizListDialog.InfoDialog("Title", "Text")

        viewModel.activeDialog.test {
            assertThat(awaitItem()).isNull() // Initial state
            
            viewModel.setDialogState(dialog)
            
            assertThat(awaitItem()).isEqualTo(dialog)
        }
    }

    @Test
    fun `clearDialogState should reset activeDialog flow to null`() = runTest {
        val dialog = QuizListDialog.CustomDialog
        viewModel.setDialogState(dialog)

        viewModel.activeDialog.test {
            assertThat(awaitItem()).isEqualTo(dialog) // Current state
            
            viewModel.clearDialogState()
            
            assertThat(awaitItem()).isNull()
        }
    }
}
