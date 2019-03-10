package com.vocabulary.myvocabulary.ui.words

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class WordListViewModel(
        val dictionaryId: Long,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val wordList = wordRepository.allWords
    private val liveWordList: MutableLiveData<List<Word>> = MutableLiveData()
    private val defaultWordId = 1L
    private val isWordInDictionary: Observable<Boolean> = wordRepository.isWordInDictionary
    private val isDefaultWordInDictionary: MutableLiveData<Boolean> = MutableLiveData()

    init {
        wordRepository.observeAllList(dictionaryId)
        wordRepository.observeIfWordIsInDictionary(defaultWordId)
        observeList()
        observeIfWordInDictionary()
    }

    fun insertWord(word: Word) {
        disposables += Completable.fromCallable { wordRepository.createWord(word) }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe()
    }

    private fun observeList() {
        disposables += wordList
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { liveWordList.postValue(it) }
    }

    private fun observeIfWordInDictionary() {
        disposables += isWordInDictionary
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { isDefaultWordInDictionary.postValue(it) }
    }

    fun isDefaultWordSet(): LiveData<Boolean> = isDefaultWordInDictionary

    fun getLiveWordList(): LiveData<List<Word>> = liveWordList

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }
}