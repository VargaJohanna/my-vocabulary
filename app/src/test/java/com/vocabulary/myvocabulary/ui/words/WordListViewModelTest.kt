package com.vocabulary.myvocabulary.ui.words

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.vocabulary.myvocabulary.TestScheduler
import com.vocabulary.myvocabulary.repositories.sortBy.SortByData
import com.vocabulary.myvocabulary.repositories.sortBy.SortByOptions
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import io.reactivex.Observable
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verify
import java.util.*
import java.util.Arrays.asList

class WordListViewModelTest {
    @Rule
    @JvmField
    var mockito = InstantTaskExecutorRule()
    private val dictionaryId = 2L
    private val date = Date(2010, 10, 10, 10, 10)
    private val sortByRepository = mock<SortByRepository>()
    private val wordRepository = mock<WordRepository>()
    private val sortedListRepository = mock<SortedListRepository>()
    private val quizRepository = mock<QuizRepository>()
    private val wordListToTest = asList(
            Word(containerDictionaryId = dictionaryId, word = "a", translation = "translation", created = date),
            Word(containerDictionaryId = dictionaryId, word = "b", translation = "translation2", created = date),
            Word(containerDictionaryId = dictionaryId, word = "c", translation = "translation3", created = date)
    )
    private val sortByDataToTest = SortByData(sortByOption = SortByOptions.SortByTranslation, dateDescending = true, wordDescending = true, translationDescending = false)

    @Test
    fun `should create word when insertWord() is called`() {
        val wordListViewModel = givenWordListViewModel()
        val wordToTest = Word(containerDictionaryId = dictionaryId, word = "word", translation = "translation", created = date)

        wordListViewModel.insertWord(wordToTest)

        verify(wordRepository).createWord(wordToTest)
    }

    @Test
    fun `should create word object with given word-translation pair when createWordObject() is called`() {
        val wordListViewModel = givenWordListViewModel()
        val wordString = "word"
        val translationString = "translation"

        val createdObject = wordListViewModel.createWordObject(wordString, translationString)

        assertEquals(wordString, createdObject.word)
        assertEquals(translationString, createdObject.translation)
    }

    @Test
    fun `should update word when updateWord() is called`() {
        val wordListViewModel = givenWordListViewModel()
        val wordToUpdate = Word(containerDictionaryId = dictionaryId, word = "word", translation = "translation", created = date)

        wordListViewModel.updateWord(wordToUpdate)

        verify(wordRepository).updateWord(wordToUpdate)
    }

    @Test
    fun `should delete word when deleteWord() is called`() {
        val wordListViewModel = givenWordListViewModel()
        val wordToDelete = Word(containerDictionaryId = dictionaryId, word = "word", translation = "translation", created = date)

        wordListViewModel.deleteWord(wordToDelete)

        verify(wordRepository).deleteWord(wordToDelete)
    }

    @Test
    fun `should reset quiz list when startNew() is called`() {
        val wordListViewModel = givenWordListViewModel()
        val quizType = QuizTypes.QuickQuiz

        val resetQuiz = wordListViewModel.startNew(dictionaryId, quizType)

        assertEquals(resetQuiz, quizRepository.resetQuizList(dictionaryId, quizType))
    }

    @Test
    fun `should delegate setSortBy in repository when setSortBy() is called`() {
        val wordListViewModel = givenWordListViewModel()
        val sortByData = sortByDataToTest

        wordListViewModel.setSortBy(sortByData)

        verify(sortByRepository).setSortBy(sortByData)
    }

    @Test
    fun `should return a liveData with a list of words`() {
        val wordListViewModel = givenWordListViewModelWithData()

        wordListViewModel.getLiveWordList().observeForever(mock())

        assertEquals(wordListToTest, wordListViewModel.getLiveWordList().value)
    }

    @Test
    fun `should return sortByData when currentSortByData is called`() {
        val wordListViewModel = givenWordListViewModelWithData()

        val currentSortByData = wordListViewModel.currentSortByData

        assertEquals(sortByDataToTest,
                currentSortByData)
    }

    @Test
    fun `should return true when word list is empty`() {
        val wordListViewModel = givenWordListViewModelWithEmptyList()

        wordListViewModel.isListEmpty().observeForever(mock())

        assertEquals(true, wordListViewModel.isListEmpty().value)
    }

    @Test
    fun `should return false when word list is not empty`() {
        val wordListViewModel = givenWordListViewModelWithData()

        wordListViewModel.isListEmpty().observeForever(mock())

        assertEquals(false, wordListViewModel.isListEmpty().value)
    }

    private fun givenWordListViewModel(): WordListViewModel {
        whenever(sortedListRepository.getSortedWordList(any())).thenReturn(Observable.never())
        whenever(sortByRepository.sortByData()).thenReturn(Observable.never())
        return WordListViewModel(dictionaryId, sortByRepository, wordRepository, sortedListRepository, TestScheduler(), quizRepository)
    }

    private fun givenWordListViewModelWithData(): WordListViewModel {
        whenever(sortedListRepository.getSortedWordList(dictionaryId)).thenReturn(Observable.just(wordListToTest))
        whenever(sortByRepository.sortByData()).thenReturn(Observable.just(sortByDataToTest))
        return WordListViewModel(dictionaryId, sortByRepository, wordRepository, sortedListRepository, TestScheduler(), quizRepository)
    }

    private fun givenWordListViewModelWithEmptyList(): WordListViewModel {
        whenever(sortedListRepository.getSortedWordList(dictionaryId)).thenReturn(Observable.just(emptyList()))
        whenever(sortByRepository.sortByData()).thenReturn(Observable.never())
        return WordListViewModel(dictionaryId, sortByRepository, wordRepository, sortedListRepository, TestScheduler(), quizRepository)
    }
}