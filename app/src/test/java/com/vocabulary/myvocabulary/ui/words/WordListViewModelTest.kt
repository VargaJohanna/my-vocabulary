package com.vocabulary.myvocabulary.ui.words

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.vocabulary.myvocabulary.TestScheduler
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizRepository
import org.junit.Rule
import org.junit.Test
import java.util.*

class WordListViewModelTest {
    @Rule
    @JvmField
    var mockito = InstantTaskExecutorRule()
    private val dictionaryId = 2L
    private val date = Date()

    private val sortByRepository = mock<SortByRepository>()
    private val wordRepository = mock<WordRepository>()
    private val sortedListRepository = mock<SortedListRepository>()
    private val quizRepository = mock<QuizRepository>()

    @Test
    fun`should create word when insertWord() is called`() {
        val wordListViewModel = givenWordListViewModel()
        val wordToTest = Word(containerDictionaryId = dictionaryId, word = "word", translation = "translation")

        wordListViewModel.insertWord(wordToTest)
    }

    private fun givenWordListViewModel(): WordListViewModel {
        return WordListViewModel(dictionaryId, sortByRepository, wordRepository, sortedListRepository, TestScheduler(), quizRepository)
    }
}