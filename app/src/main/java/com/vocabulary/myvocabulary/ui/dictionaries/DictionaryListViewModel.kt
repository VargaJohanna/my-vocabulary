package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class DictionaryListViewModel(
        private val dictionaryRepository: DictionaryRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {

    private val disposables = CompositeDisposable()
    //    val dictionary: Single<Dictionary> = dictionaryRepository.getDictionaryById()
    private val dictionaryList: Single<List<Dictionary>> = dictionaryRepository.getAllDictionaries()
    private val numberOfDictionaries: Single<Int> = dictionaryRepository.getNumberOfDictionaries()
    private val liveNumberOfDictionaries: MutableLiveData<Int> = MutableLiveData()
    private val liveDictionaryList: MutableLiveData<List<Dictionary>> = MutableLiveData()

    init {
        observeList()
        observeNumberOfDictionaries()
    }

    fun insertDictionary(dictionary: Dictionary) {
        disposables += Completable.fromCallable { dictionaryRepository.createDictionary(dictionary) }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe()
    }


    private fun observeList() {
        disposables.add(
                dictionaryList
                        .map {
                            liveDictionaryList.postValue(it)
                        }
                        .subscribeOn(rxSchedulers.io())
                        .observeOn(rxSchedulers.main())
                        .subscribe())
    }

    fun getDictionaryList(): LiveData<List<Dictionary>> {
        return liveDictionaryList
    }

    private fun observeNumberOfDictionaries() {
        disposables.add(
                numberOfDictionaries
                        .map {
                            liveNumberOfDictionaries.postValue(it)
                        }
                        .subscribeOn(rxSchedulers.io())
                        .observeOn(rxSchedulers.main())
                        .subscribe())
    }

    fun getNumberOfDictionaries(): LiveData<Int> {
        return liveNumberOfDictionaries
    }


    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }
}
