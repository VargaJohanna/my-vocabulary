package com.vocabulary.myvocabulary.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.DispatcherProvider
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.search.SearchRepository
import com.vocabulary.myvocabulary.repositories.sortBy.SortByData
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

class WordListViewModel(
        val dictionaryId: Long,
        private val sortByRepository: SortByRepository,
        private val wordRepository: WordRepository,
        private val sortedListRepository: SortedListRepository,
        private val quizRepository: QuizRepository,
        private val searchRepository: SearchRepository,
        private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _wordListUiState = MutableStateFlow<WordListUiState>(WordListUiState.Loading)
    val wordListUiState: StateFlow<WordListUiState> = _wordListUiState.asStateFlow()

    private val _activeDialog = MutableStateFlow<WordListDialog?>(null)
    val activeDialog = _activeDialog.asStateFlow()

    private val _isSheetOpen = MutableStateFlow(false)
    val isSheetOpen = _isSheetOpen.asStateFlow()

    private val _clickedWord = MutableStateFlow<Word?>(null)
    val clickedWord = _clickedWord.asStateFlow()

    var currentSortByData: SortByData = SortByData()

    init {
        viewModelScope.launch {
            observeList()
            observeSortByData()
        }
    }

    private fun observeSortByData() {
        viewModelScope.launch {
            sortByRepository.sortByData().asFlow().collect {
                currentSortByData = it
            }
        }
    }

    fun insertWord(word: Word) {
        viewModelScope.launch(dispatchers.io) {
            try {
                wordRepository.createWord(word)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _wordListUiState.value = WordListUiState.Error("Failed to insert word. Error: ${e.message}")
            }
        }
    }

    private fun observeList() {
        viewModelScope.launch {
            _wordListUiState.value = WordListUiState.Loading

            try {
                val wordsFlow = sortedListRepository.getSortedWordList(dictionaryId).asFlow()
                val searchFlow = searchRepository.searchedTerm.asFlow()

                combine(wordsFlow, searchFlow) { wordList, searchTerm ->
                    val filteredList = searchList(wordList, searchTerm)
                    if (filteredList.isEmpty()) {
                        WordListUiState.Empty
                    } else {
                        WordListUiState.Success(wordList = filteredList)
                    }
                }
                .flowOn(dispatchers.default)
                .collect { newState ->
                    _wordListUiState.value = newState
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _wordListUiState.value = WordListUiState.Error("Failed to observe list. Error: ${e.message}")
            }
        }
    }

    fun createWordObject(word: String, translation: String) = Word(containerDictionaryId = dictionaryId,
            word = word,
            translation = translation,
            created = Calendar.getInstance().time)

    fun updateWord(word: Word) {
        viewModelScope.launch(dispatchers.io) {
            wordRepository.updateWord(word)
        }
    }

    fun deleteWord(word: Word) {
        viewModelScope.launch(dispatchers.io) {
            wordRepository.deleteWord(word)
        }
    }

    fun startNew(dictionaryId: Long, quizType: QuizTypes) {
        viewModelScope.launch{
            quizRepository.setQuizList(dictionaryId, quizType)
        }
    }

    fun setSortBy(sortByData: SortByData) {
        sortByRepository.setSortBy(sortByData)
    }

    private fun searchList(wordList: List<Word>, find: String): List<Word> {
        return wordList.filter { it.translation.contains(find, true) || it.word.contains(find, true) }
    }

    fun setSearchedTerm(searchTerm: String) {
        searchRepository.setSearchedTerm(searchTerm)
    }

    fun setActiveDialog(dialog: WordListDialog) {
        _activeDialog.value = dialog
    }

    fun clearActiveDialog() {
        _activeDialog.value = null
    }

    fun setSheetOpen(isOpen: Boolean) {
        _isSheetOpen.value = isOpen
    }

    fun setClickedWord(word: Word?) {
        _clickedWord.value = word
    }
}

sealed class WordListDialog {
    object Create : WordListDialog()
    data class Edit(val word: Word) : WordListDialog()
    data class Delete(val word: Word) : WordListDialog()
}

sealed interface WordListUiState {
    object Loading : WordListUiState
    object Empty : WordListUiState

    data class Error(
        val message: String
    ) : WordListUiState

    data class Success(
        val wordList: List<Word>
    ) : WordListUiState
}
