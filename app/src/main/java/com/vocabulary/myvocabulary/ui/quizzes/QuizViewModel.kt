package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.disposables.CompositeDisposable


class QuizViewModel(
        val dictionaryId: Long,
        val optionType: Int,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val liveWordList: MutableLiveData<List<FocusableWord>> = MutableLiveData()
    private var focusableWordList: MutableList<FocusableWord> = mutableListOf()
    private val updateIcon: MutableLiveData<Boolean> = MutableLiveData()
    private var lastIndexOfSubList: Int = 1
    private var listIsFinished = false
    val directionType = optionType.toDirectionType()

    init {
        observeList()
    }

    private fun observeList() {
        disposables += wordRepository.getObservableWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe {
                    it.forEachIndexed { index: Int, word: Word ->
                        focusableWordList.add(FocusableWord(word, index == 0))
                    }
                    liveWordList.postValue(focusableWordList.subList(0, 1))
                    if (focusableWordList.size == 1) {
                        updateIcon.postValue(true)
                        listIsFinished = true
                    }
                }
    }

    fun getLiveWordList(): LiveData<List<FocusableWord>> = liveWordList

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun nextClicked() {
        if (lastIndexOfSubList < focusableWordList.size) {
            lastIndexOfSubList += 1
            setFocusableValue(lastIndexOfSubList)
            listIsFinished = lastIndexOfSubList == focusableWordList.size

            liveWordList.postValue(focusableWordList.subList(0, lastIndexOfSubList))
            updateIcon.postValue(lastIndexOfSubList == focusableWordList.size)
        }
    }

    fun listIsNotFinished() = !listIsFinished
    fun getUpdateIcon(): LiveData<Boolean> = updateIcon

    private fun setFocusableValue(position: Int) {
        focusableWordList.subList(0, lastIndexOfSubList).forEachIndexed { index, focusableWord ->
            val focused = index == position - 1 || index == position - 2
            focusableWordList.subList(0, lastIndexOfSubList)[index] = focusableWord.copy(isFocused = focused)
        }
    }

    data class FocusableWord(
            val word: Word,
            val isFocused: Boolean
    )

    data class GuessedWord(
            val wordId: Long,
            val guess: String,
            val wordValue: String
    )
}