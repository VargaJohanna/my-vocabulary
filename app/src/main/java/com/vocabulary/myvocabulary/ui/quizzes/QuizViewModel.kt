package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


class QuizViewModel(
        val dictionaryId: Long,
        val optionType: String,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val liveWordList: MutableLiveData<List<FocusableWord>> = MutableLiveData()
    private var focusableWordList: MutableList<FocusableWord> = emptyList<FocusableWord>().toMutableList()
    private val updateIcon: MutableLiveData<Boolean> = MutableLiveData()
    private var toIndexOfSubList: Int = 1
    private var listIsFinished = false

    init {
        observeList()
    }

    private fun observeList() {
        disposables += wordRepository.getObservableWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe {
                    it.forEachIndexed { index: Int, word: Word ->
                        when (index) {
                            0 -> focusableWordList.add(FocusableWord(word, true))
                            else -> focusableWordList.add(FocusableWord(word, false))
                        }
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

    operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }

    fun nextClicked() {
        if (toIndexOfSubList < focusableWordList.size) {
            toIndexOfSubList += 1
            setFocusableValue(toIndexOfSubList)
            liveWordList.postValue(focusableWordList.subList(0, toIndexOfSubList))
            if (toIndexOfSubList == focusableWordList.size) {
                updateIcon.postValue(true)
                listIsFinished = true
            } else {
                updateIcon.postValue(false)
                listIsFinished = false
            }
        }
    }

    fun getListIsFinished() = listIsFinished
    fun getUpdateIcon(): LiveData<Boolean> = updateIcon
    fun isMeaning(): Boolean = optionType.capitalize().equals(MEANING)

    companion object {
        const val MEANING = "Meaning"
    }

    private fun setFocusableValue(position: Int) {
        focusableWordList.subList(0, toIndexOfSubList).forEachIndexed { index, focusableWord ->
            when (index) {
                position - 1 -> focusableWordList.subList(0, toIndexOfSubList)[index] = focusableWord.copy(isFocused = true)
                position - 2 -> focusableWordList.subList(0, toIndexOfSubList)[index] = focusableWord.copy(isFocused = true)
                else -> focusableWordList.subList(0, toIndexOfSubList)[index] = focusableWord.copy(isFocused = false)
            }
        }
    }

    data class FocusableWord(
            val word: Word,
            val isFocused: Boolean
    )

    data class GuessedWord(
            val wordId: Long,
            val guess: String
    )
}