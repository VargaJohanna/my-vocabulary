package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.utils.Event
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.*

class DictionaryListViewModel(
        private val dictionaryRepository: DictionaryRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val liveDictionaryList: MutableLiveData<List<Dictionary>> = MutableLiveData()
    private val liveNumberOfDictionaries: MutableLiveData<Int> = MutableLiveData()
    private val _newlyCreatedItemDetails = MutableLiveData<Event<Map<String, String>>>()
    val newlyCreatedItemDetails: LiveData<Event<Map<String, String>>> = _newlyCreatedItemDetails

    init {
        observeList()
        observeNumberOfDictionaries()
    }

    fun insertDictionary(dictionary: Dictionary) {
        disposables += Single.fromCallable { dictionaryRepository.createDictionary(dictionary) }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { t: Long ->
                    _newlyCreatedItemDetails.value = Event(mapOf(ID_KEY to t.toString(), ID_NAME to dictionary.dictionaryName))
                }
    }

    fun insertDefaultDictionary(dictionary: Dictionary) {
        disposables += Single.fromCallable { dictionaryRepository.createDictionary(dictionary) }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe()
    }

    private fun observeList() {
        disposables += dictionaryRepository.allDictionaries
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { liveDictionaryList.postValue(it) }
    }

    fun getDictionaryList(): LiveData<List<Dictionary>> {
        return liveDictionaryList
    }

    private fun observeNumberOfDictionaries() {
        disposables += dictionaryRepository.numberOfDictionaries
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { liveNumberOfDictionaries.postValue(it) }
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

    fun createDictionaryObject(dictionaryName: String): Dictionary = Dictionary(dictionaryName = dictionaryName,
            dictionaryCreated = Calendar.getInstance().time)

    fun renameDictionary(dictionary: Dictionary) {
        disposables += Completable.fromCallable {
            dictionaryRepository.updateDictionary(dictionary)
        }.subscribeOn(rxSchedulers.io())
                .subscribe()
    }

    fun deleteDictionary(dictionary: Dictionary) {
        disposables += Completable.fromCallable {
            dictionaryRepository.deleteDictionary(dictionary)
        }.subscribeOn(rxSchedulers.io())
                .subscribe()
    }

    companion object {
        const val ID_KEY = "key"
        const val ID_NAME = "name"
    }
}
