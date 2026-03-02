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

    fun fetchQuizList() {
        _isLoading.value = true
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
                        _quizList.value = filteredList.shuffled()
                    } else {
                        _quizList.value = emptyList()
                    }
                    _isLoading.value = false
                },
                { error ->
                    Log.e("QuizViewModel", "Error fetching quiz list", error)
                    _isLoading.value = false
                }
            )
    }

    public override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    data class FocusableWord(
        val word: Word,
        val isFocused: Boolean
    )
}
