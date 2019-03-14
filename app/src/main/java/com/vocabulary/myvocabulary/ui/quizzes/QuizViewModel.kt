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
    private val livePositionOfLastQuestion: MutableLiveData<Int> = MutableLiveData()
    var positionOfLastQuestion: Int = 0


    init {
        observeList()
    }

    private fun observeList() {
        disposables += wordRepository.getObservableWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { liveWordList.postValue(it) }
    }

    fun getLiveWordList(): LiveData<List<Word>> = liveWordList

    fun updatePositionOfLastQuestion(position: Int) {
        livePositionOfLastQuestion.postValue(position)
    }

    fun getLivePositionOfLastQuestion(): LiveData<Int> = livePositionOfLastQuestion


    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }

}