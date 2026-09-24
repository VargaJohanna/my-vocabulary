package com.vocabulary.myvocabulary.ui.home

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.vocabulary.myvocabulary.quotes.QuoteData
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.quotes.QuoteRepository
import com.vocabulary.myvocabulary.repositories.share.ShareDictionaryRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.testing.MainCoroutineRule
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val quoteRepository: QuoteRepository = mockk(relaxed = true)
    private val shareDictionaryRepository: ShareDictionaryRepository = mockk(relaxed = true)
    private val dictionaryRepository: DictionaryRepository = mockk(relaxed = true)
    private val preferences: SharedPreferences = mockk(relaxed = true)
    private val dataStore: DataStore<Preferences> = mockk(relaxed = true)
    private val wordRepository: WordRepository = mockk(relaxed = true)

    private val dictionariesFlow = MutableStateFlow<List<Dictionary>>(emptyList())
    private val testQuote = QuoteData.Quote("Test Quote", "Author", "Work")

    private val dict1 = Dictionary(
        dictionaryId = 1L,
        dictionaryName = "Spanish",
        dictionaryCreated = Date(1000),
        dictionaryLastPracticed = Date(2000),
        dictionaryLastResult = 80,
        dictionaryFinishedCount = 5,
        dictionaryTotalScore = 400
    )

    private val dict2 = Dictionary(
        dictionaryId = 2L,
        dictionaryName = "French",
        dictionaryCreated = Date(500),
        dictionaryLastPracticed = Date(3000),
        dictionaryLastResult = 90,
        dictionaryFinishedCount = 10,
        dictionaryTotalScore = 900
    )

    @Before
    fun setup() {
        val mockPreferences: Preferences = mockk(relaxed = true)
        every { mockPreferences[any<Preferences.Key<Int>>()] } returns 0
        every { dataStore.data } returns flowOf(mockPreferences)
        every { dictionaryRepository.allDictionaries } returns dictionariesFlow
        every { dictionaryRepository.isSyncing } returns MutableStateFlow(false)
        every { quoteRepository.getQuote() } returns flowOf(testQuote)
        coEvery { wordRepository.getObservableWordList(any()) } returns flowOf(emptyList())
    }

    @Test
    fun `homeUiState should emit Loading initially then Success after quote and stats load`() = runTest {
        dictionariesFlow.value = listOf(dict1, dict2)

        val viewModel = HomeViewModel(
            quoteRepository,
            shareDictionaryRepository,
            dictionaryRepository,
            preferences,
            dataStore,
            wordRepository
        )

        viewModel.homeUiState.test {
            assertThat(awaitItem()).isInstanceOf(HomeUiState.Loading::class)
            val success = awaitItem()
            assertThat(success).isInstanceOf(HomeUiState.Success::class)
            val data = success as HomeUiState.Success
            assertThat(data.numOfDictionaries).isEqualTo(2)
            assertThat(data.lastPracticed?.dictionaryId).isEqualTo(2L)
        }
    }

    @Test
    fun `homeUiState should remain Loading while isSyncing is true`() = runTest {
        val syncingFlow = MutableStateFlow(true)
        every { dictionaryRepository.isSyncing } returns syncingFlow

        val viewModel = HomeViewModel(
            quoteRepository,
            shareDictionaryRepository,
            dictionaryRepository,
            preferences,
            dataStore,
            wordRepository
        )

        viewModel.homeUiState.test {
            assertThat(awaitItem()).isInstanceOf(HomeUiState.Loading::class)

            syncingFlow.value = false

            val success = awaitItem()
            assertThat(success).isInstanceOf(HomeUiState.Success::class)
        }
    }

    @Test
    fun `dictionary stats should calculate last, most, and least practiced dictionaries correctly`() = runTest {
        dictionariesFlow.value = listOf(dict1, dict2)

        val viewModel = HomeViewModel(
            quoteRepository,
            shareDictionaryRepository,
            dictionaryRepository,
            preferences,
            dataStore,
            wordRepository
        )

        advanceUntilIdle()

        viewModel.lastPracticedDictionary.test {
            val last = awaitItem()
            assertThat(last).isNotNull()
            assertThat(last?.dictionaryId).isEqualTo(2L)
        }

        viewModel.mostPracticedDictionary.test {
            val most = awaitItem()
            assertThat(most).isNotNull()
            assertThat(most?.dictionaryId).isEqualTo(2L)
        }

        viewModel.leastPracticedDictionary.test {
            val least = awaitItem()
            assertThat(least).isNotNull()
            assertThat(least?.dictionaryId).isEqualTo(1L)
        }
    }

    @Test
    fun `dictionary stats should be null when dictionary list is empty`() = runTest {
        dictionariesFlow.value = emptyList()

        val viewModel = HomeViewModel(
            quoteRepository,
            shareDictionaryRepository,
            dictionaryRepository,
            preferences,
            dataStore,
            wordRepository
        )

        advanceUntilIdle()

        viewModel.lastPracticedDictionary.test {
            assertThat(awaitItem()).isNull()
        }

        viewModel.mostPracticedDictionary.test {
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun `quoteUiState should emit Loading then Success when quoteRepository returns quote`() = runTest {
        val viewModel = HomeViewModel(
            quoteRepository,
            shareDictionaryRepository,
            dictionaryRepository,
            preferences,
            dataStore,
            wordRepository
        )

        viewModel.quoteUiState.test {
            assertThat(awaitItem()).isInstanceOf(QuoteUiState.Loading::class)
            val state = awaitItem()
            assertThat(state).isInstanceOf(QuoteUiState.Success::class)
            assertThat((state as QuoteUiState.Success).quote).isEqualTo(testQuote)
        }
    }
}
