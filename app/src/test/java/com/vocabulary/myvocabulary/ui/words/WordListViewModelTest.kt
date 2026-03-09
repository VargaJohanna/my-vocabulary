package com.vocabulary.myvocabulary.ui.words

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.search.SearchRepository
import com.vocabulary.myvocabulary.repositories.sortBy.SortByData
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import io.mockk.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class WordListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dictionaryId = 1L
    private val sortByRepository: SortByRepository = mockk()
    private val wordRepository: WordRepository = mockk()
    private val sortedListRepository: SortedListRepository = mockk()
    private val quizRepository: QuizRepository = mockk()
    private val searchRepository: SearchRepository = mockk()
    private val rxSchedulers: RxSchedulers = mockk()

    private lateinit var viewModel: WordListViewModel

    @Before
    fun setup() {
        // Setup Schedulers for RxJava to run immediately
        every { rxSchedulers.io() } returns Schedulers.trampoline()
        every { rxSchedulers.main() } returns Schedulers.trampoline()

        // Setup Coroutine Dispatcher for viewModelScope
        Dispatchers.setMain(UnconfinedTestDispatcher())

        viewModel = WordListViewModel(
            dictionaryId,
            sortByRepository,
            wordRepository,
            sortedListRepository,
            rxSchedulers,
            quizRepository,
            searchRepository
        )
    }

    @Test
    fun `fetchWordList should observe list and sortByData`() {
        // Arrange
        val mockWords = listOf(Word(dictionaryId, dictionaryId, "Hello", "Szia", 0, 0, 0, Date()))
        val mockSortBy = SortByData(wordDescending = true)

        every { sortedListRepository.getSortedWordList(dictionaryId) } returns Observable.just(mockWords)
        every { searchRepository.searchedTerm } returns Observable.just("")
        every { sortByRepository.sortByData() } returns Observable.just(mockSortBy)

        // Act
        viewModel.fetchWordList()

        // Assert
        assertEquals(mockWords, viewModel.wordList.value.first)
        assertEquals(mockSortBy, viewModel.currentSortByData)
    }

    @Test
    fun `insertWord should call repository createWord`() {
        // Arrange
        val word = viewModel.createWordObject("Apple", "Alma")
        every { wordRepository.createWord(any()) } returns Unit

        // Act
        viewModel.insertWord(word)

        // Assert
        verify { wordRepository.createWord(word) }
    }

    @Test
    fun `deleteWord should call repository deleteWord`() {
        // Arrange
        val word = viewModel.createWordObject("Apple", "Alma")
        every { wordRepository.deleteWord(any()) } returns Unit

        // Act
        viewModel.deleteWord(word)

        // Assert
        verify { wordRepository.deleteWord(word) }
    }

    @Test
    fun `updateWord should call repository updateWord`() {
        // Arrange
        val word = viewModel.createWordObject("Apple", "Alma")
        every { wordRepository.updateWord(any()) } returns Unit

        // Act
        viewModel.updateWord(word)

        // Assert
        verify { wordRepository.updateWord(word) }
    }

    @Test
    fun `startNew should trigger quiz repository reset`() {
        // Arrange
        val quizType = QuizTypes.FullQuiz
        every { quizRepository.resetQuizList(dictionaryId, quizType) } returns Completable.complete()

        // Act
        viewModel.startNew(dictionaryId, quizType)

        // Assert
        verify { quizRepository.resetQuizList(dictionaryId, quizType) }
    }

    @Test
    fun `setSearchedTerm should update search repository`() {
        // Arrange
        val term = "find me"
        every { searchRepository.setSearchedTerm(term) } returns Unit

        // Act
        viewModel.setSearchedTerm(term)

        // Assert
        verify { searchRepository.setSearchedTerm(term) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Clears the test dispatcher
    }
}