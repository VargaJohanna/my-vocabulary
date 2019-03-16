package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class QuizViewModel(
        val dictionaryId: Long,
        val optionType: String,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val liveWordList: MutableLiveData<List<Word>> = MutableLiveData()
    private val updateIcon: MutableLiveData<Boolean> = MutableLiveData()
    private var positionOfNextQuestion: Int = 1
    private lateinit var wordList: List<Word>
    private var listIsFinished = false

    init {
        observeList()
    }

    private fun observeList() {
        disposables += wordRepository.getObservableWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { wordList = it
                liveWordList.postValue(it.subList(0, 1))
                    if(wordList.size == 1) {
                        updateIcon.postValue(true)
                        listIsFinished = true
                    }}
    }

    fun getLiveWordList(): LiveData<List<Word>> = liveWordList

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }

    fun nextClicked() {
        if(positionOfNextQuestion < wordList.size) {
            positionOfNextQuestion += 1
            liveWordList.postValue(wordList.subList(0, positionOfNextQuestion))
            if(positionOfNextQuestion == wordList.size) {
                updateIcon.postValue(true)
                listIsFinished = true
            } else {
                updateIcon.postValue(false)
                listIsFinished = false
            }
        }
    }
    fun getListIsFinished() = listIsFinished
    fun getUpdateIcon() :LiveData<Boolean> = updateIcon
    fun isMeaning():Boolean = optionType.capitalize().equals(MEANING)

    companion object {
        const val MEANING = "Meaning"
    }

}