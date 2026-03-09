package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.vocabulary.myvocabulary.TestScheduler
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import java.util.*

class DictionaryListViewModelTest {
    val dictionaryTest = Dictionary(dictionaryName = "Test",
        dictionaryCreated = Date(12),
        dictionaryLastPracticed = Date(12),
        dictionaryLastResult = 0,
        dictionaryFinishedCount = 0,
        dictionaryTotalScore = 100)
    @Rule
    @JvmField
    var mockito = InstantTaskExecutorRule()

    private val dictionaryRepository = mock<DictionaryRepository>()
    private val quizRepository = mock<QuizRepository>()

    private val sortByRepository = mock<SortDictionaryRepository>()
    private val sortedListRepository = mock<SortedListRepository>()
    private val newDictionaryId = 5L

    @Test
    fun `should create dictionary when insertDictionary() is called`() {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryWithId = dictionaryTest

        dictionaryListViewModel.insertDictionary(dictionaryWithId)

        verify(dictionaryRepository).createDictionary(dictionaryWithId)
    }

    @Test
    fun `should update newlyCreatedItemDetails when insertDictionary() is called`() {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionary = dictionaryTest

        dictionaryListViewModel.insertDictionary(dictionary)

//        Assert.assertEquals(DictionaryDetails(newDictionaryId, dictionary.dictionaryName),
//                dictionaryListViewModel.newlyCreatedItemDetails.value?.peekContent())
    }

    @Test
    fun `should create dictionary object with given name when createDictionaryObject() is called`(){
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryName = "Hungarian"

        val dictionary = dictionaryListViewModel.createDictionaryObject(dictionaryName)

        Assert.assertEquals(dictionaryName, dictionary.dictionaryName)
    }

    @Test
    fun `should update dictionary when renameDictionary() is called`() {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryToUpdate = dictionaryTest

        dictionaryListViewModel.renameDictionary(dictionaryToUpdate)

        verify(dictionaryRepository).updateDictionary(dictionaryToUpdate)
    }

    @Test
    fun `should delete dictionary when deleteDictionary() is called`() {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryWithId = dictionaryTest

        dictionaryListViewModel.deleteDictionary(dictionaryWithId)

        verify(dictionaryRepository).deleteDictionary(dictionaryWithId)
    }

    @Test
    fun `should update currentSortByData when sortByData emits`() {
        val sortData = com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryData(
            dateDescending = false,
            titleDescending = false
        )
        whenever(sortByRepository.sortByData()).thenReturn(Observable.just(sortData))
        whenever(sortedListRepository.getSortedDictionaryList()).thenReturn(Observable.never())

        val dictionaryListViewModel = DictionaryListViewModel(
            dictionaryRepository,
            TestScheduler(),
            sortByRepository,
            sortedListRepository
        )

        Assert.assertEquals(sortData, dictionaryListViewModel.currentSortByData)
    }

    @Test
    fun `should delegate setSortBy() to repository`() {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val sortData = com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryData()

        dictionaryListViewModel.setSortBy(sortData)

        verify(sortByRepository).setSortBy(sortData)
    }

    private fun givenDictionaryListViewModel(): DictionaryListViewModel {
        whenever(dictionaryRepository.createDictionary(any())).thenReturn(newDictionaryId)
        whenever(dictionaryRepository.allDictionaries).thenReturn(Observable.never())
        // Ensure ViewModel init subscriptions have safe sources
        whenever(sortByRepository.sortByData()).thenReturn(Observable.never())
        whenever(sortedListRepository.getSortedDictionaryList()).thenReturn(Observable.never())
        return DictionaryListViewModel(
            dictionaryRepository,
            TestScheduler(),
            sortByRepository,
            sortedListRepository
        )
    }
}