package com.vocabulary.myvocabulary.ui.dictionaries

import assertk.assertThat
import assertk.assertions.isFalse
import com.nhaarman.mockitokotlin2.*
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.testing.MainCoroutineRule
import com.vocabulary.myvocabulary.testing.TestDispatchers
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class DictionaryListViewModelTest {
    val dictionaryTest = Dictionary(dictionaryName = "Test",
        dictionaryCreated = Date(12),
        dictionaryLastPracticed = Date(12),
        dictionaryLastResult = 0,
        dictionaryFinishedCount = 0,
        dictionaryTotalScore = 100)
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val dictionaryRepository = mock<DictionaryRepository>()

    private val sortByRepository = mock<SortDictionaryRepository>()
    private val sortedListRepository = mock<SortedListRepository>()
    private val newDictionaryId = 5L
    private lateinit var testDispatcher: TestDispatchers

    @Before
    fun setup() {
        testDispatcher = TestDispatchers(mainCoroutineRule.testDispatcher)
    }

    @Test
    fun `should create dictionary when insertDictionary() is called`() = runTest {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryWithId = dictionaryTest
        assertThat(dictionaryListViewModel.isLoading.value).isFalse()

        dictionaryListViewModel.insertDictionary(dictionaryWithId)
        
        advanceUntilIdle()

        verify(dictionaryRepository).createDictionary(dictionaryWithId)
        assertThat(dictionaryListViewModel.isLoading.value).isFalse()
    }

    @Test
    fun `should update newlyCreatedItemDetails when insertDictionary() is called`() = runTest {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionary = dictionaryTest

        dictionaryListViewModel.insertDictionary(dictionary)
        advanceUntilIdle()
        Assert.assertEquals(DictionaryDetails(newDictionaryId, dictionary.dictionaryName),
                dictionaryListViewModel.newDictionary.value.peekContent())
    }

    @Test
    fun `should create dictionary object with given name when createDictionaryObject() is called`() = runTest {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryName = "Hungarian"

        val dictionary = dictionaryListViewModel.createDictionaryObject(dictionaryName)

        Assert.assertEquals(dictionaryName, dictionary.dictionaryName)
    }

    @Test
    fun `should update dictionary when renameDictionary() is called`() = runTest {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryToUpdate = dictionaryTest

        dictionaryListViewModel.renameDictionary(dictionaryToUpdate)
        advanceUntilIdle()

        verify(dictionaryRepository).updateDictionary(dictionaryToUpdate)
    }

    @Test
    fun `should delete dictionary when deleteDictionary() is called`() = runTest {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryWithId = dictionaryTest

        dictionaryListViewModel.deleteDictionary(dictionaryWithId)
        advanceUntilIdle()

        verify(dictionaryRepository).deleteDictionary(dictionaryWithId)
    }

    @Test
    fun `should update currentSortByData when sortByData emits`() = runTest {
        val sortData = com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryData(
            dateDescending = false,
            titleDescending = false
        )
        whenever(sortByRepository.sortByData()).thenReturn(Observable.just(sortData))
        whenever(sortedListRepository.getSortedDictionaryList()).thenReturn(Observable.never())

        val dictionaryListViewModel = DictionaryListViewModel(
            dictionaryRepository,
            sortByRepository,
            sortedListRepository,
            testDispatcher
        )
        advanceUntilIdle()

        Assert.assertEquals(sortData, dictionaryListViewModel.currentSortByData)
    }

    @Test
    fun `should delegate setSortBy() to repository`() = runTest {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val sortData = com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryData()

        dictionaryListViewModel.setSortBy(sortData)
        advanceUntilIdle()

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
            sortByRepository,
            sortedListRepository,
            testDispatcher
        )
    }
}