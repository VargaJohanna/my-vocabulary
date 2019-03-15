package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class QuizViewModel(
        val dictionaryId: Long,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val liveWordList: MutableLiveData<List<Word>> = MutableLiveData()
    private var positionOfLastQuestion: Int = 1
    private lateinit var wordList: List<Word>

    init {
        observeList()
    }

    private fun observeList() {
        disposables += wordRepository.getObservableWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { wordList = it
                liveWordList.postValue(it.subList(0, 1))}
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
        if(positionOfLastQuestion + 1 < wordList.size) {
            positionOfLastQuestion += 1
            liveWordList.postValue(wordList.subList(0, positionOfLastQuestion))
        }
    }
}