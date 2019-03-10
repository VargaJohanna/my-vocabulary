package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.*

class DictionaryListViewModel(
        private val dictionaryRepository: DictionaryRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val dictionaryList = dictionaryRepository.allDictionaries
    private val liveDictionaryList: MutableLiveData<List<Dictionary>> = MutableLiveData()
    private val numberOfDictionaries = dictionaryRepository.numberOfDictionaries
    private val liveNumberOfDictionaries: MutableLiveData<Int> = MutableLiveData()
    private var createdId: Long = 0

    init {
        observeList()
        observeNumberOfDictionaries()
    }

    fun insertDictionary(dictionary: Dictionary) {
        disposables += Single.fromCallable {
            dictionaryRepository.createDictionary(dictionary)
        }
                .map { createdId = it }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe()
    }

    fun getCreatedId() = createdId

    private fun observeList() {
        disposables.add(
                dictionaryList
                        .subscribeOn(rxSchedulers.io())
                        .observeOn(rxSchedulers.main())
                        .subscribe { liveDictionaryList.postValue(it) })
    }

    fun getDictionaryList(): LiveData<List<Dictionary>> {
        return liveDictionaryList
    }

    private fun observeNumberOfDictionaries() {
        disposables.add(
                numberOfDictionaries
                        .subscribeOn(rxSchedulers.io())
                        .observeOn(rxSchedulers.main())
                        .subscribe { liveNumberOfDictionaries.postValue(it) })
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

    fun createDictionary(dictionaryName: String): Dictionary = Dictionary(dictionaryName, Calendar.getInstance().time)

}
