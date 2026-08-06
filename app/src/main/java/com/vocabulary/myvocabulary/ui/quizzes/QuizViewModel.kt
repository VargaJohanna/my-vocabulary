package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuizViewModel(
    val dictionaryId: Long,
    val isFailedOnly: Boolean,
    private val rxSchedulers: RxSchedulers,
    private val quizRepository: QuizRepository
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _quizList: MutableStateFlow<List<Word>> = MutableStateFlow(emptyList())
    val quizList: StateFlow<List<Word>> = _quizList
    var isDictionaryEmpty = false
    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _quizUiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val quizUiState: StateFlow<QuizUiState> = _quizUiState.asStateFlow()


    fun fetchQuizList() {
        if (_quizUiState.value is QuizUiState.SuccessList) return
        _quizUiState.value = QuizUiState.Loading
        observeQuizList(isFailedOnly)
    }

    fun startQuiz(quizType: QuizTypes, dictionaryId: Long) {
        if (_quizUiState.value is QuizUiState.SuccessList) return
        viewModelScope.launch {
            if (isFailedOnly.not()) {
                quizRepository.setQuizList(dictionaryId, quizType)
                    .subscribe()
            }
        }
    }

    private fun observeQuizList(failedOnly: Boolean) {
        disposables += quizRepository.quizList
            .subscribeOn(rxSchedulers.io())
            .observeOn(rxSchedulers.main())
            .subscribe(
                { list ->
                    isDictionaryEmpty = list.isEmpty()
                    if (list.isNotEmpty()) {
                        val filteredShuffledList = list.filter { word ->
                            val isValid = word.word.isNotBlank() && word.translation.isNotBlank()
                            val matchesCriteria = if (failedOnly) word.lastResult.not() else true
                            isValid && matchesCriteria
                        }.shuffled()

                        if (filteredShuffledList.isNotEmpty()) {
                            _quizUiState.value = QuizUiState.SuccessList(
                                quizList = filteredShuffledList,
                                currentFocusedWordId = filteredShuffledList.first().wordId,
                                isFabIconNext = filteredShuffledList.size > 1
                            )
                        } else {
                            _quizUiState.value = QuizUiState.EmptyList
                        }
                    } else {
                        _quizUiState.value = QuizUiState.EmptyList
                    }
                },
                { error ->
                    _quizUiState.value =
                        QuizUiState.Error(error.message ?: "Error fetching quiz list")
                }
            )
    }

    fun onNextClicked() {
        _quizUiState.update { currentState ->
            if (currentState is QuizUiState.SuccessList) {
                if (currentState.rollingIndex >= currentState.quizList.size) {
                    return@update currentState
                }
                val nextIndex = currentState.rollingIndex + 1
                val hasMoreWords = nextIndex < currentState.quizList.size

                val incrementedState = currentState.copy(
                    rollingIndex = nextIndex,
                    isFabIconNext = hasMoreWords,
                    currentGuess = "",
                )
                updateFocusedWord(incrementedState)
            } else {
                currentState
            }
        }
    }

    fun onGuessChanged(guess: String) {
        _quizUiState.update { currentState ->
            if (currentState is QuizUiState.SuccessList) {
                currentState.copy(currentGuess = guess)
            } else currentState
        }
    }

    private fun updateFocusedWord(state: QuizUiState.SuccessList): QuizUiState.SuccessList {
        val wordId = state.quizList.getOrNull(state.rollingIndex - 1)?.wordId ?: 0L
        return state.copy(currentFocusedWordId = wordId)
    }

    public override fun onCleared() {
        _quizUiState.value = QuizUiState.SuccessList(
            quizList = emptyList()
        )
        disposables.clear()
        super.onCleared()
    }
}

sealed interface QuizUiState {
    object Loading : QuizUiState
    object EmptyList : QuizUiState
    data class SuccessList(
        val quizList: List<Word>,
        val rollingIndex: Int = 1,
        val currentGuess: String = "",
        val isFabIconNext: Boolean = true,
        val currentFocusedWordId: Long = 0L,
    ) : QuizUiState

    data class Error(val message: String) : QuizUiState
}