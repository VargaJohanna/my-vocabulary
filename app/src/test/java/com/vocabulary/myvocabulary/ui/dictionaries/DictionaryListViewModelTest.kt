package com.vocabulary.myvocabulary.ui.dictionaries

import android.content.ContentResolver
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryData
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.testing.MainCoroutineRule
import com.vocabulary.myvocabulary.testing.TestDispatchers
import io.mockk.*
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class DictionaryListViewModelTest {
    private val dictionaryTest = Dictionary(
        dictionaryName = "Test",
        dictionaryCreated = Date(12),
        dictionaryLastPracticed = Date(12),
        dictionaryLastResult = 0,
        dictionaryFinishedCount = 0,
        dictionaryTotalScore = 100
    )

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val dictionaryRepository: DictionaryRepository = mockk(relaxed = true)
    private val sortByRepository: SortDictionaryRepository = mockk(relaxed = true)
    private val sortedListRepository: SortedListRepository = mockk(relaxed = true)
    private val shareDictViewModel: ShareDictionaryViewModel = mockk(relaxed = true)
    private val contentResolver: ContentResolver = mockk(relaxed = true)
    
    private val newDictionaryId = 5L
    private lateinit var testDispatcher: TestDispatchers

    @Before
    fun setup() {
        testDispatcher = TestDispatchers(mainCoroutineRule.testDispatcher)
        
        // Default stubs for init block
        every { dictionaryRepository.allDictionaries } returns Observable.never()
        every { sortByRepository.sortByData() } returns Observable.never()
        every { sortedListRepository.getSortedDictionaryList() } returns Observable.never()
        every { shareDictViewModel.importedDictionaryDetailsFlow } returns MutableSharedFlow()
    }

    @Test
    fun `libraryUiState should emit Loading then LibraryData when repository emits items`() = runTest {
        val list = listOf(dictionaryTest)
        every { sortedListRepository.getSortedDictionaryList() } returns Observable.just(list)

        val viewModel = givenDictionaryListViewModel()

        viewModel.libraryUiState.test {
            assertThat(awaitItem()).isInstanceOf(LibraryUiState.Loading::class)
            val state = awaitItem()
            assertThat(state).isInstanceOf(LibraryUiState.LibraryData::class)
            assertThat((state as LibraryUiState.LibraryData).dictionaryList).isEqualTo(list)
        }
    }

    @Test
    fun `libraryUiState should emit Empty when repository emits an empty list`() = runTest {
        every { sortedListRepository.getSortedDictionaryList() } returns Observable.just(emptyList())

        val viewModel = givenDictionaryListViewModel()

        viewModel.libraryUiState.test {
            assertThat(awaitItem()).isInstanceOf(LibraryUiState.Loading::class)
            assertThat(awaitItem()).isInstanceOf(LibraryUiState.Empty::class)
        }
    }

    @Test
    fun `libraryUiState should emit Error when repository fails`() = runTest {
        every { sortedListRepository.getSortedDictionaryList() } returns Observable.error(Exception("DB Crash"))

        val viewModel = givenDictionaryListViewModel()
        
        viewModel.libraryUiState.test {
            assertThat(awaitItem()).isInstanceOf(LibraryUiState.Loading::class)
            val state = awaitItem()
            assertThat(state).isInstanceOf(LibraryUiState.Error::class)
            assertThat((state as LibraryUiState.Error).message).isEqualTo("Failed to observe list. Error: DB Crash")
        }
    }

    @Test
    fun `should create dictionary when insertDictionary() is called`() = runTest {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryWithId = dictionaryTest
        
        coEvery { dictionaryRepository.createDictionary(any()) } returns newDictionaryId
        every { sortedListRepository.getSortedDictionaryList() } returns Observable.just(listOf(dictionaryWithId))

        dictionaryListViewModel.insertDictionary(dictionaryWithId)
        
        advanceUntilIdle()

        verify { dictionaryRepository.createDictionary(dictionaryWithId) }
        assertThat(dictionaryListViewModel.libraryUiState.value).isInstanceOf(LibraryUiState.LibraryData::class)
    }

    @Test
    fun `should update createDictionaryEvent when insertDictionary() is called`() = runTest {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionary = dictionaryTest
        
        coEvery { dictionaryRepository.createDictionary(any()) } returns newDictionaryId

        dictionaryListViewModel.insertDictionary(dictionary)
        advanceUntilIdle()
        
        Assert.assertEquals(DictionaryDetails(newDictionaryId, dictionary.dictionaryName),
                dictionaryListViewModel.createDictionaryEvent.value?.peekContent())
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

        verify { dictionaryRepository.updateDictionary(dictionaryToUpdate) }
    }

    @Test
    fun `should delete dictionary when deleteDictionary() is called`() = runTest {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryWithId = dictionaryTest

        dictionaryListViewModel.deleteDictionary(dictionaryWithId)
        advanceUntilIdle()

        verify { dictionaryRepository.deleteDictionary(dictionaryWithId) }
    }

    @Test
    fun `should update currentSortByData when sortByData emits`() = runTest {
        val sortData = SortDictionaryData(
            dateDescending = false,
            titleDescending = false
        )
        every { sortByRepository.sortByData() } returns Observable.just(sortData)

        val dictionaryListViewModel = DictionaryListViewModel(
            dictionaryRepository,
            sortByRepository,
            sortedListRepository,
            testDispatcher,
            shareDictViewModel,
            contentResolver
        )
        advanceUntilIdle()

        Assert.assertEquals(sortData, dictionaryListViewModel.currentSortByData)
    }

    @Test
    fun `should delegate setSortBy() to repository`() = runTest {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val sortData = SortDictionaryData()

        dictionaryListViewModel.setSortBy(sortData)
        advanceUntilIdle()

        verify { sortByRepository.setSortBy(sortData) }
    }

    private fun givenDictionaryListViewModel(): DictionaryListViewModel {
        return DictionaryListViewModel(
            dictionaryRepository,
            sortByRepository,
            sortedListRepository,
            testDispatcher,
            shareDictViewModel,
            contentResolver
        )
    }
}
