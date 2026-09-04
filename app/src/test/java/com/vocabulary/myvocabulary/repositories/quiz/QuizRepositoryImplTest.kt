package com.vocabulary.myvocabulary.repositories.quiz

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.ui.words.Word
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class QuizRepositoryImplTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val wordRepository = mockk<WordRepository>()
    private val customQuizRepository = mockk<CustomQuizRepository>(relaxed = true)
    private val dictionaryId = 1L
    private val date = Date(5)
    private val wordListToTest = listOf(
            Word(containerDictionaryId = dictionaryId, word = "a", translation = "translation1", created = date),
            Word(containerDictionaryId = dictionaryId, word = "b", translation = "translation2", created = date),
            Word(containerDictionaryId = dictionaryId, word = "c", translation = "translation3", created = date),
            Word(containerDictionaryId = dictionaryId, word = "d", translation = "translation4", created = date),
            Word(containerDictionaryId = dictionaryId, word = "e", translation = "translation5", created = date),
            Word(containerDictionaryId = dictionaryId, word = "f", translation = "translation6", created = date),
            Word(containerDictionaryId = dictionaryId, word = "g", translation = "translation7", created = date),
            Word(containerDictionaryId = dictionaryId, word = "h", translation = "translation8", created = date)
    )

    @Test
    fun `should emit full list when quiz type is FullQuiz`() = runTest {
        // Arrange
        val quizRepository = givenQuizRepositoryWithData()

        // Act
        quizRepository.setQuizList(dictionaryId, QuizTypes.FullQuiz)

        // Assert
        quizRepository.quizList.test {
            assertThat(awaitItem()).isEqualTo(wordListToTest)
        }
    }

    @Test
    fun `should emit 5 words when quiz type is WeakestQuiz`() = runTest {
        // Arrange
        val quizRepository = givenQuizRepositoryWithData()

        // Act
        quizRepository.setQuizList(dictionaryId, QuizTypes.WeakestQuiz)

        // Assert
        quizRepository.quizList.test {
            val list = awaitItem()
            assertThat(list.size).isEqualTo(5)
        }
    }

    @Test
    fun `should update quizList when setQuizList() is called with QuickQuiz quiz type`() = runTest{
        val quizRepository = givenQuizRepositoryWithData()
        
        quizRepository.setQuizList(dictionaryId, QuizTypes.QuickQuiz)

        quizRepository.quizList.test {
            val list = awaitItem()
            assertThat(list.size).isEqualTo(5)
        }
    }

    @Test
    fun `should update quizList when updateQuizList() is called`() = runTest {
        val quizRepository = givenQuizRepository()
        
        quizRepository.updateQuizList(wordListToTest)

        quizRepository.quizList.test {
            assertThat(awaitItem()).isEqualTo(wordListToTest)
        }
    }

    private fun givenQuizRepository(): QuizRepository {
        return QuizRepositoryImpl(wordRepository, customQuizRepository)
    }

    private fun givenQuizRepositoryWithData(): QuizRepository {
        every { wordRepository.getObservableWordList(dictionaryId) } returns Observable.just(wordListToTest)
        return QuizRepositoryImpl(wordRepository, customQuizRepository)
    }
}