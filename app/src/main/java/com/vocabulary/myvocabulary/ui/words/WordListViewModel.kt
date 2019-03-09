package com.vocabulary.myvocabulary.ui.words

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class WordListViewModel(
        private val dictionaryId: Long,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val wordList: Single<List<Word>>? = wordRepository.getAllWords(dictionaryId)
    private val liveWordList: MutableLiveData<List<Word>> = MutableLiveData()
    private val defaultWordId = 1L
    private val numberOfWordsWithId1: Single<Int> = wordRepository.isWordIdInDictionary(defaultWordId)
    private val isDefaultWordInDictionary: MutableLiveData<Boolean> = MutableLiveData()

    init {
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
        if (wordList != null) {
            disposables.add(
                    wordList
                            .map {
                                liveWordList.postValue(it)
                            }
                            .subscribeOn(rxSchedulers.io())
                            .observeOn(rxSchedulers.main())
                            .subscribe())
        }
    }

    private fun observeIfWordInDictionary() {
        disposables.add(
                numberOfWordsWithId1
                        .map {
                            isDefaultWordInDictionary.postValue(it != 0)
                        }
                        .subscribeOn(rxSchedulers.io())
                        .observeOn(rxSchedulers.main())
                        .subscribe())
    }

    fun isDefaultWordSet(): LiveData<Boolean> = isDefaultWordInDictionary

    fun getLiveWordList(): LiveData<List<Word>> = liveWordList

    fun getDictionaryId(): Long = dictionaryId

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }
}