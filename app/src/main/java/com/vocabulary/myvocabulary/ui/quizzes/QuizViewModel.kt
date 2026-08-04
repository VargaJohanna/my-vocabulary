package com.vocabulary.myvocabulary.ui.quizzes

import android.util.Log
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
import kotlinx.coroutines.launch

class QuizViewModel(
    val dictionaryId: Long,
    val failedOnly: Boolean,
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
//        _isLoading.value = true
        _quizUiState.value = QuizUiState.Loading
        observeQuizList(failedOnly)
    }

    fun startQuiz(quizType: QuizTypes, dictionaryId: Long) {
        viewModelScope.launch {
            if (failedOnly.not()) {
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
                        val filteredList = list.filter { word ->
                            val isValid = word.word.isNotBlank() && word.translation.isNotBlank()
                            val matchesCriteria = if (failedOnly) !word.lastResult else true
                            isValid && matchesCriteria
                        }.shuffled()
                        _quizUiState.value = QuizUiState.SuccessList(
                            quizList = filteredList.shuffled(),
                        )
//                        _quizList.value = filteredList.shuffled()
                    } else {
//                        _quizList.value = emptyList()
                        _quizUiState.value = QuizUiState.EmptyList
                    }
//                    _isLoading.value = false
                },
                { error ->
//                    Log.e("QuizViewModel", "Error fetching quiz list", error)
                    _quizUiState.value =
                        QuizUiState.Error(error.message ?: "Error fetching quiz list")
//                    _isLoading.value = false
                }
            )
    }

    fun onNextClicked() {
        val currentState = _quizUiState.value
        if (currentState is QuizUiState.SuccessList) {
            val updateState = currentState.copy(
                isNextClicked = true,
                rollingIndex = currentState.rollingIndex + 1,
                isCardActive = false,
                isFabIconNext = currentState.quizList.size == currentState.rollingIndex + 1
            )
        }
    }

    public override fun onCleared() {
//        _quizList.value = emptyList()
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
        val isCardActive: Boolean = true,
        val isCardLast: Boolean = false,
        val rollingIndex: Int = 1,
        val guessContent: String = "",
        val isFabIconNext: Boolean = true,
        val isNextClicked: Boolean = false,
        val focusedWordId: Long = 0L
    ) : QuizUiState

    data class Error(val message: String) : QuizUiState
}