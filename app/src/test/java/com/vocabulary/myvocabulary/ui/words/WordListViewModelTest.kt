package com.vocabulary.myvocabulary.ui.words

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.search.SearchRepository
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.testing.MainCoroutineRule
import com.vocabulary.myvocabulary.testing.TestDispatchers
import io.mockk.*
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class WordListViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val dictionaryId = 1L
    private val sortByRepository: SortByRepository = mockk(relaxed = true)
    private val wordRepository: WordRepository = mockk(relaxed = true)
    private val sortedListRepository: SortedListRepository = mockk(relaxed = true)
    private val quizRepository: QuizRepository = mockk(relaxed = true)
    private val searchRepository: SearchRepository = mockk(relaxed = true)
    
    private lateinit var viewModel: WordListViewModel
    private lateinit var testDispatcher: TestDispatchers

    @Before
    fun setup() {
        testDispatcher = TestDispatchers(mainCoroutineRule.testDispatcher)
        
        // Default stubs for init block
        every { sortByRepository.sortByData() } returns Observable.never()
        every { sortedListRepository.getSortedWordList(dictionaryId) } returns Observable.never()
        every { searchRepository.searchedTerm } returns Observable.never()
    }

    @Test
    fun `wordListUiState should emit Loading then Success when repository emits items`() = runTest {
        val words = listOf(Word(1, dictionaryId, "word", "translation", 0, 0, 0, Date()))
        every { sortedListRepository.getSortedWordList(dictionaryId) } returns Observable.just(words)
        every { searchRepository.searchedTerm } returns Observable.just("")

        viewModel = givenWordListViewModel()

        viewModel.wordListUiState.test {
            assertThat(awaitItem()).isInstanceOf(WordListUiState.Loading::class)
            val state = awaitItem()
            assertThat(state).isInstanceOf(WordListUiState.Success::class)
            assertThat((state as WordListUiState.Success).wordList).isEqualTo(words)
        }
    }

    @Test
    fun `wordListUiState should emit Empty when filtered list is empty`() = runTest {
        every { sortedListRepository.getSortedWordList(dictionaryId) } returns Observable.just(emptyList())
        every { searchRepository.searchedTerm } returns Observable.just("")

        viewModel = givenWordListViewModel()

        viewModel.wordListUiState.test {
            assertThat(awaitItem()).isInstanceOf(WordListUiState.Loading::class)
            assertThat(awaitItem()).isInstanceOf(WordListUiState.Empty::class)
        }
    }

    @Test
    fun `should call wordRepository createWord when insertWord is called`() = runTest {
        viewModel = givenWordListViewModel()
        val word = viewModel.createWordObject("word", "translation")

        viewModel.insertWord(word)
        advanceUntilIdle()

        verify { wordRepository.createWord(word) }
    }

    @Test
    fun `setActiveDialog should update activeDialog flow`() = runTest {
        viewModel = givenWordListViewModel()
        val word = Word(1, dictionaryId, "word", "translation", 0, 0, 0, Date())
        val dialog = WordListDialog.Edit(word)

        viewModel.activeDialog.test {
            assertThat(awaitItem()).isEqualTo(null)
            viewModel.setActiveDialog(dialog)
            assertThat(awaitItem()).isEqualTo(dialog)
        }
    }

    private fun givenWordListViewModel(): WordListViewModel {
        return WordListViewModel(
            dictionaryId = dictionaryId,
            sortByRepository = sortByRepository,
            wordRepository = wordRepository,
            sortedListRepository = sortedListRepository,
            quizRepository = quizRepository,
            searchRepository = searchRepository,
            dispatchers = testDispatcher
        )
    }
}
