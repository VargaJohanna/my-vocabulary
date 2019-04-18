package com.vocabulary.myvocabulary.ui.quizzes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.vocabulary.myvocabulary.TestScheduler
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.*
import java.util.Arrays.asList

class QuizViewModelTest {
    @Rule
    @JvmField
    var mockito = InstantTaskExecutorRule()

    private val quizRepository = mock<QuizRepository>()
    private val dictionaryId = 1L
    private val optionType = 1
    private val failedOnly = false
    private val date = Date(2010, 10, 10, 10, 10)
    private val quizList = asList(
            Word(containerDictionaryId = dictionaryId, word = "a", translation = "translation", created = date),
            Word(containerDictionaryId = dictionaryId, word = "b", translation = "translation2", created = date),
            Word(containerDictionaryId = dictionaryId, word = "c", translation = "translation3", created = date)
    )

    @Test
    fun `should return list of words when getLiveWordList() is called`() {
        val quizViewModel = givenQuizViewModel()

        quizViewModel.getLiveWordList().observeForever(mock())

        assertEquals(asList(quizList.map {QuizViewModel.FocusableWord(it, false)}.last()), quizViewModel.getLiveWordList().value)
    }

    private fun givenQuizViewModel(): QuizViewModel {
        whenever(quizRepository.quizList).thenReturn(Observable.just(quizList))
        return QuizViewModel(dictionaryId, optionType, failedOnly, TestScheduler(), quizRepository)
    }
}