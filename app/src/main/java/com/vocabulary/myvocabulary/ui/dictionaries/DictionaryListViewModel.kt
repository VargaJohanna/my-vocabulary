package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.quiz.CustomQuizRepository
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryData
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.utils.Event
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class DictionaryListViewModel(
        private val dictionaryRepository: DictionaryRepository,
        private val rxSchedulers: RxSchedulers,
        private val quizRepository: QuizRepository,
        private val sortByRepository: SortDictionaryRepository,
        private val sortedListRepository: SortedListRepository,
        private val customQuizRepository: CustomQuizRepository

) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val _liveDictionaryList: MutableLiveData<List<Dictionary>> = MutableLiveData()
    val liveDictionaryList: LiveData<List<Dictionary>> = _liveDictionaryList
    private val _newlyCreatedItemDetails = MutableLiveData<Event<DictionaryDetails>>()
    val newlyCreatedItemDetails: LiveData<Event<DictionaryDetails>> = _newlyCreatedItemDetails
    private lateinit var dictionaryName: String
    var currentSortByData: SortDictionaryData = SortDictionaryData()
    private val isListEmpty = MutableLiveData<Boolean>()

    init {
        observeList()
        observeSortByData()
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
        disposables += sortedListRepository.getSortedDictionaryList()
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe {
                    _liveDictionaryList.postValue(it)
                    isListEmpty.value = it.isEmpty()
                }
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

    fun startNew(dictionaryId: Long, quizType: QuizTypes): Completable {
        return quizRepository.resetQuizList(dictionaryId, quizType, customQuizRepository.quizSize)
    }

    fun setDictionaryTitle(title: String) {
        dictionaryName = title
    }

    private fun observeSortByData() {
        disposables += sortByRepository.sortByData()
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { t -> currentSortByData = t }
    }

    fun setSortBy(sortByData: SortDictionaryData) {
        sortByRepository.setSortBy(sortByData)
    }

    fun isListEmpty(): LiveData<Boolean> = isListEmpty

    fun addCustomQuizSize(size: Int?) {
        size?.let {
            customQuizRepository.quizSize = size
        }
    }
}
