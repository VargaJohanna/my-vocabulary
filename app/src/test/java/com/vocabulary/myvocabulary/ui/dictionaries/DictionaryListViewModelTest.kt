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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
    private val sortedListFlow = MutableStateFlow<List<Dictionary>>(emptyList())

    @Before
    fun setup() {
        testDispatcher = TestDispatchers(mainCoroutineRule.testDispatcher)
        
        // Default stubs for init block
        every { dictionaryRepository.allDictionaries } returns flowOf(emptyList())
        every { sortByRepository.sortByData() } returns flowOf(SortDictionaryData())
        every { sortedListRepository.getSortedDictionaryList() } returns sortedListFlow
        every { shareDictViewModel.importedDictionaryDetailsFlow } returns MutableSharedFlow()
    }

    @Test
    fun `libraryUiState should emit Loading then LibraryData when repository emits items`() = runTest {
        val list = listOf(dictionaryTest)
        
        val viewModel = givenDictionaryListViewModel()

        viewModel.libraryUiState.test {
            assertThat(awaitItem()).isInstanceOf(LibraryUiState.Loading::class)
            // State should be Empty initially due to sortedListFlow default
            assertThat(awaitItem()).isInstanceOf(LibraryUiState.Empty::class)
            
            sortedListFlow.value = list
            
            val state = awaitItem()
            assertThat(state).isInstanceOf(LibraryUiState.LibraryData::class)
            assertThat((state as LibraryUiState.LibraryData).dictionaryList).isEqualTo(list)
        }
    }

    @Test
    fun `libraryUiState should emit Empty when repository emits an empty list`() = runTest {
        val viewModel = givenDictionaryListViewModel()

        viewModel.libraryUiState.test {
            assertThat(awaitItem()).isInstanceOf(LibraryUiState.Loading::class)
            assertThat(awaitItem()).isInstanceOf(LibraryUiState.Empty::class)
        }
    }

    @Test
    fun `libraryUiState should emit Error when repository fails`() = runTest {
        every { sortedListRepository.getSortedDictionaryList() } returns flow { throw Exception("DB Crash") }

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
        val viewModel = givenDictionaryListViewModel()
        val dictionaryWithId = dictionaryTest
        
        coEvery { dictionaryRepository.createDictionary(any()) } returns newDictionaryId

        viewModel.libraryUiState.test {
            // Skip initial states (Loading, Empty)
            assertThat(awaitItem()).isInstanceOf(LibraryUiState.Loading::class)
            assertThat(awaitItem()).isInstanceOf(LibraryUiState.Empty::class)

            viewModel.insertDictionary(dictionaryWithId)
            
            // Should transition to Loading during insertion
            assertThat(awaitItem()).isInstanceOf(LibraryUiState.Loading::class)
            
            // Simulate repository update after insertion
            sortedListFlow.value = listOf(dictionaryWithId)
            
            assertThat(awaitItem()).isInstanceOf(LibraryUiState.LibraryData::class)
        }

        coVerify { dictionaryRepository.createDictionary(dictionaryWithId) }
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

        coVerify { dictionaryRepository.updateDictionary(dictionaryToUpdate) }
    }

    @Test
    fun `should delete dictionary when deleteDictionary() is called`() = runTest {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryWithId = dictionaryTest

        dictionaryListViewModel.deleteDictionary(dictionaryWithId)
        advanceUntilIdle()

        coVerify { dictionaryRepository.deleteDictionary(dictionaryWithId) }
    }

    @Test
    fun `should update currentSortByData when sortByData emits`() = runTest {
        val sortData = SortDictionaryData(
            dateDescending = false,
            titleDescending = false
        )
        val sortByFlow = MutableStateFlow(SortDictionaryData())
        every { sortByRepository.sortByData() } returns sortByFlow

        val dictionaryListViewModel = DictionaryListViewModel(
            dictionaryRepository,
            sortByRepository,
            sortedListRepository,
            testDispatcher,
            shareDictViewModel,
            contentResolver
        )
        
        sortByFlow.value = sortData
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
