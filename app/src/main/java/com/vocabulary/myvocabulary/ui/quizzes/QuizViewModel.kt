package com.vocabulary.myvocabulary.ui.quizzes

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    val optionType: Int,
    val failedOnly: Boolean,
    private val rxSchedulers: RxSchedulers,
    private val quizRepository: QuizRepository
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val updateIcon = MutableLiveData<Boolean>()
    private val isFabIconUpdated = MutableStateFlow(false)
    private var lastIndexOfSubList = 1
    private var listIsFinished = false
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

                        updateIcon.postValue(getFocusableWordList().size == 1)
                        listIsFinished = getFocusableWordList().size == 1
                        isFabIconUpdated.value = getFocusableWordList().size == 1
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

    fun nextClicked() {
        if (lastIndexOfSubList < getFocusableWordList().size) {
            lastIndexOfSubList += 1
            setFocusableValue(lastIndexOfSubList)
            listIsFinished = lastIndexOfSubList == getFocusableWordList().size
            updateIcon.postValue(lastIndexOfSubList == getFocusableWordList().size)
            isFabIconUpdated.value = lastIndexOfSubList == getFocusableWordList().size
        }
    }

    fun listIsNotFinished() = !listIsFinished

    fun getUpdateIcon(): LiveData<Boolean> = updateIcon

    fun isFabIconUpdated(): MutableStateFlow<Boolean> = isFabIconUpdated

    private fun setFocusableValue(position: Int) {
//        getFocusableWordList().subList(0, lastIndexOfSubList).forEachIndexed { index, focusableWord ->
//            val focused = index == position - 1 || index == position - 2
//            getFocusableWordList().subList(0, lastIndexOfSubList)[index] = focusableWord.copy(isFocused = focused)
//        }
    }

    @VisibleForTesting
    fun getFocusableWordList() = emptyList<FocusableWord>()

    @VisibleForTesting
    fun setFocusableWordList(list: MutableList<FocusableWord>) {
//        focusableWordList = list
    }

    @VisibleForTesting
    fun getListIsFinished() = listIsFinished

    data class FocusableWord(
        val word: Word,
        val isFocused: Boolean
    )
}
