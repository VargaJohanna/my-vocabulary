package com.vocabulary.myvocabulary.repositories.quiz

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.ui.words.Word
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.Completable
import org.junit.Rule
import org.junit.Test
import java.util.*

class QuizRepositoryImplTest {
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val wordRepository = mockk<WordRepository>()
    private val customQuizRepository = mockk<CustomQuizRepository>()
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
    fun `should emit full list when quiz type is FullQuiz`() {
        // Arrange
        val quizRepository = givenQuizRepositoryWithData()

        // Act
        quizRepository.resetQuizList(dictionaryId, QuizTypes.FullQuiz).blockingGet()

        // Assert
        quizRepository.quizList.test()
            .assertValue(wordListToTest)
            .assertNoErrors()
    }

    @Test
    fun `should emit 5 words when quiz type is WeakestQuiz`() {
        // Arrange
        // Mock the dependency method name based on your repository
        every { customQuizRepository.getWeakestQuizList(any()) } returns wordListToTest.take(5)
        val quizRepository = givenQuizRepositoryWithData()

        // Act
        quizRepository.resetQuizList(dictionaryId, QuizTypes.WeakestQuiz).blockingGet()

        // Assert
        quizRepository.quizList.test()
            .assertValue { it.size == 5 }
    }

    @Test
    fun `should update quizList when resetQuizList() is called with QuickQuiz quiz type`() {
        val quizRepository = givenQuizRepositoryWithData()
        quizRepository.resetQuizList(dictionaryId, QuizTypes.QuickQuiz).blockingGet()

        val testObserver = quizRepository.quizList.test()

        testObserver.assertValue { it.size == 5 }
                .assertNoErrors()
                .dispose()
    }

    @Test
    fun `should update quizList when updateQuizList() is called`() {
        val quizRepository = givenQuizRepository()
        quizRepository.updateQuizList(wordListToTest)

        val testObserver = quizRepository.quizList.test()

        testObserver.assertValue(wordListToTest)
                .assertNoErrors()
                .dispose()
    }

    private fun givenQuizRepository(): QuizRepository {
        whenever(wordRepository.getObservableWordList(dictionaryId)).thenReturn(Observable.never())
        return QuizRepositoryImpl(wordRepository, customQuizRepository)
    }

    private fun givenQuizRepositoryWithData(): QuizRepository {
        // MockK syntax: 'every { ... } returns ...'
        every { wordRepository.getObservableWordList(dictionaryId) } returns Observable.just(wordListToTest)

        return QuizRepositoryImpl(wordRepository, customQuizRepository)
    }
}