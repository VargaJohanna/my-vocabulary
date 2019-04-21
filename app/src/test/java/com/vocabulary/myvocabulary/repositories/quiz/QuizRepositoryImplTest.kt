package com.vocabulary.myvocabulary.repositories.quiz

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import org.junit.Rule
import org.junit.Test
import java.util.*
import java.util.Arrays.asList

class QuizRepositoryImplTest {
    @Rule
    @JvmField
    var mockito = InstantTaskExecutorRule()

    private val wordRepository = mock<WordRepository>()
    private val dictionaryId = 1L
    private val date = Date(5)
    private val wordListToTest = asList(
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
    fun `should update quizList when resetQuizList() is called with FullQuiz quiz type`() {
        val quizRepository = givenQuizRepositoryWithData()
        quizRepository.resetQuizList(dictionaryId, QuizTypes.FullQuiz).blockingGet()

        val testObserver = quizRepository.quizList.test()

        testObserver.assertValue(wordListToTest)
                .assertNoErrors()
                .dispose()
    }

    @Test
    fun `should update quizList when resetQuizList() is called with WeakestQuiz quiz type`() {
        val quizRepository = givenQuizRepositoryWithData()
        quizRepository.resetQuizList(dictionaryId, QuizTypes.WeakestQuiz).blockingGet()

        val testObserver = quizRepository.quizList.test()

        testObserver.assertValue { it.size == 5 }
                .assertNoErrors()
                .dispose()
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
        return QuizRepositoryImpl(wordRepository)
    }

    private fun givenQuizRepositoryWithData(): QuizRepository {
        whenever(wordRepository.getObservableWordList(dictionaryId)).thenReturn(Observable.just(wordListToTest))
        return QuizRepositoryImpl(wordRepository)
    }
}