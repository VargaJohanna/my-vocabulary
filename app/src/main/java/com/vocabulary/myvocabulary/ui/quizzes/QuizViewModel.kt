package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.ui.words.Word
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await

class QuizViewModel(
    val dictionaryId: Long,
    val isFailedOnly: Boolean,
    private val quizRepository: QuizRepository,
) : ViewModel() {
    private val _quizUiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val quizUiState: StateFlow<QuizUiState> = _quizUiState.asStateFlow()
    private var quizCollectionJob: Job? = null
    private val _events = Channel<QuizEvent>()
    val events = _events.receiveAsFlow()

    fun fetchQuizList() {
        if (_quizUiState.value is QuizUiState.SuccessList) {
            return
        }
        _quizUiState.value = QuizUiState.Loading
        observeQuizList(isFailedOnly)
    }

    fun startQuiz(quizType: QuizTypes, dictionaryId: Long) {
        if (_quizUiState.value is QuizUiState.SuccessList) return
        viewModelScope.launch {
            if (isFailedOnly.not()) {
                try {
                    quizRepository.setQuizList(dictionaryId, quizType).await()
                } catch (e: Exception) {
                    _quizUiState.value = QuizUiState.Error("Failed to initialize quiz. Error: $e")
                }
            }
        }
    }

    private fun observeQuizList(failedOnly: Boolean) {
        quizCollectionJob?.cancel()

        quizCollectionJob = viewModelScope.launch{
            quizRepository.quizList
                .asFlow()
                .catch { error ->
                    _quizUiState.value =
                        QuizUiState.Error(error.message ?: "Error fetching quiz list")
                }
                .collect { list ->
                    val sampleWord = list.firstOrNull()
                    //Ignore the list if it belongs to a different dictionary, e.g. from a previous quiz.
                    if (sampleWord != null && sampleWord.containerDictionaryId != dictionaryId) {
                        return@collect
                    }
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
                }
        }
    }

    fun onNextClicked() {
        _quizUiState.update { currentState ->
            if (currentState is QuizUiState.SuccessList) {
                if (currentState.rollingIndex >= currentState.quizList.size) {
                    viewModelScope.launch {
                        _events.send(QuizEvent.NavigateToResult)
                    }
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

    fun clearList() {
        _quizUiState.value = QuizUiState.Loading
    }
}

sealed interface QuizUiState {
    data object Loading : QuizUiState
    data object EmptyList : QuizUiState
    data class SuccessList(
        val quizList: List<Word>,
        val rollingIndex: Int = 1,
        val currentGuess: String = "",
        val isFabIconNext: Boolean = true,
        val currentFocusedWordId: Long = 0L,
    ) : QuizUiState

    data class Error(val message: String) : QuizUiState
}

sealed interface QuizEvent {
    data object NavigateToResult : QuizEvent
}