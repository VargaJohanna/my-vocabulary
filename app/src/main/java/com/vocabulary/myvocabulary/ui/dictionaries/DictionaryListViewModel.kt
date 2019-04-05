package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.QuizRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.utils.Event
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class DictionaryListViewModel(
        private val dictionaryRepository: DictionaryRepository,
        private val rxSchedulers: RxSchedulers,
        private val quizRepository: QuizRepository
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val _liveDictionaryList: MutableLiveData<List<Dictionary>> = MutableLiveData()
    val liveDictionaryList: LiveData<List<Dictionary>> = _liveDictionaryList
    private val _newlyCreatedItemDetails = MutableLiveData<Event<DictionaryDetails>>()
    val newlyCreatedItemDetails: LiveData<Event<DictionaryDetails>> = _newlyCreatedItemDetails
    private lateinit var dictionaryName: String

    init {
        observeList()
    }

    fun insertDictionary(dictionary: Dictionary) {
        disposables += Single.fromCallable { dictionaryRepository.createDictionary(dictionary) }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { t: Long ->
                    _newlyCreatedItemDetails.value = Event(DictionaryDetails(t, dictionary.dictionaryName))
                }
    }

    private fun observeList() {
        disposables += dictionaryRepository.allDictionaries
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { _liveDictionaryList.postValue(it) }
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
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

    fun startNew(dictionaryId: Long, quizType: QuizTypes) {
        quizRepository.resetQuizList(dictionaryId, quizType)
    }

    fun setDictionaryTitle(title: String) {
        dictionaryName = title
    }
}
