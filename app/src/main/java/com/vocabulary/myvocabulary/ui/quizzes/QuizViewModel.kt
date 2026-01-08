package com.vocabulary.myvocabulary.ui.quizzes

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class QuizViewModel(
        val dictionaryId: Long,
        val optionType: Int,
        failedOnly: Boolean,
        private val rxSchedulers: RxSchedulers,
        private val quizRepository: QuizRepository
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val updateIcon = MutableLiveData<Boolean>()
    private var lastIndexOfSubList = 1
    private var listIsFinished = false
    val directionType = optionType.toDirectionType()
    private val liveSubWordList = MutableLiveData<List<FocusableWord>>()
    private val subWordList: MutableStateFlow<List<FocusableWord>> = MutableStateFlow(emptyList())
    private val _quizList: MutableStateFlow<List<FocusableWord>> = MutableStateFlow(emptyList())
    val quizList: StateFlow<List<FocusableWord>> = _quizList
    private var focusableWordList = mutableListOf<FocusableWord>()
    var isDictionaryEmpty = false

    init {
        observeQuizList(failedOnly)
    }

    private fun observeQuizList(failedOnly: Boolean) {
        disposables += quizRepository.quizList
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe {
                    isDictionaryEmpty = it.isEmpty()
                    focusableWordList.clear()
                    it.forEachIndexed { index: Int, word: Word ->
                        val newFocusableWord = QuizViewModel.FocusableWord(word, index == 0)
                        if (!getFocusableWordList().contains(newFocusableWord) && word.containerDictionaryId == dictionaryId) {
                            if (!failedOnly || !word.lastResult) getFocusableWordList().add(newFocusableWord)
                        }
                    }
                    if (getFocusableWordList().isNotEmpty()) {
                        focusableWordList.shuffle()
                        liveSubWordList.postValue(getFocusableWordList().subList(0, 1))
                        updateIcon.postValue(getFocusableWordList().size == 1)
                        listIsFinished = getFocusableWordList().size == 1
                    } else {
                        liveSubWordList.postValue(emptyList())
                    }
                }
    }

    fun getLiveWordList(): LiveData<List<FocusableWord>> = liveSubWordList

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun nextClicked() {
        if (lastIndexOfSubList < getFocusableWordList().size) {
            lastIndexOfSubList += 1
            setFocusableValue(lastIndexOfSubList)
            listIsFinished = lastIndexOfSubList == getFocusableWordList().size

            liveSubWordList.postValue(getFocusableWordList().subList(0, lastIndexOfSubList))
            updateIcon.postValue(lastIndexOfSubList == getFocusableWordList().size)
        }
    }

    fun listIsNotFinished() = !listIsFinished

    fun getUpdateIcon(): LiveData<Boolean> = updateIcon

    private fun setFocusableValue(position: Int) {
        getFocusableWordList().subList(0, lastIndexOfSubList).forEachIndexed { index, focusableWord ->
            val focused = index == position - 1 || index == position - 2
            getFocusableWordList().subList(0, lastIndexOfSubList)[index] = focusableWord.copy(isFocused = focused)
        }
    }

    @VisibleForTesting
    fun getFocusableWordList() = focusableWordList

    @VisibleForTesting
    fun setFocusableWordList(list: MutableList<FocusableWord>) {
        focusableWordList = list
    }

    @VisibleForTesting
    fun getListIsFinished() = listIsFinished

    data class FocusableWord(
            val word: Word,
            val isFocused: Boolean
    )
}
