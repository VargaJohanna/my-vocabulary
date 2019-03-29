package com.vocabulary.myvocabulary.ui.words

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import io.reactivex.disposables.CompositeDisposable

class WordDetailsViewModel(
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val currentWordLive: MutableLiveData<Word> = MutableLiveData()
    lateinit var currentWord: Word

    fun getWordById(id: Long) {
        disposables += wordRepository.getWordById(id)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { t -> currentWordLive.postValue(t) }
    }

    fun getCurrentWord(): LiveData<Word> = currentWordLive

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}