package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class DictionaryListViewModel(
        private val dictionaryRepository: DictionaryRepository,
        private val rxSchedulers: RxSchedulers,
        private val quizRepository: QuizRepository,
        private val sortByRepository: SortDictionaryRepository,
        private val sortedListRepository: SortedListRepository,
        private val customQuizRepository: CustomQuizRepository
) : ViewModel() {
    private val _dictionaries: MutableStateFlow<List<Dictionary>> = MutableStateFlow(emptyList())
    val dictionaries: StateFlow<List<Dictionary>> = _dictionaries
    private val disposables = CompositeDisposable()
    private val _liveDictionaryList: MutableLiveData<List<Dictionary>> = MutableLiveData()
    val liveDictionaryList: LiveData<List<Dictionary>> = _liveDictionaryList
    private val _newlyCreatedItemDetails = MutableLiveData<Event<DictionaryDetails>>()
    val newlyCreatedItemDetails: LiveData<Event<DictionaryDetails>> = _newlyCreatedItemDetails
    private val _newDictionary = MutableStateFlow<Event<DictionaryDetails?>>(Event(null)
    )
    val newDictionary: StateFlow<Event<DictionaryDetails?>> = _newDictionary
    private lateinit var dictionaryName: String
    var currentSortByData: SortDictionaryData = SortDictionaryData()
    private val isListEmpty = MutableLiveData<Boolean>()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    init {
        observeSortByData()
    }
    fun fetchDictionaries() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                observeList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun insertDictionary(dictionary: Dictionary) {
        disposables += Single.fromCallable { dictionaryRepository.createDictionary(dictionary) }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { t: Long ->
                    val details = DictionaryDetails(t, dictionary.dictionaryName)
                    _newlyCreatedItemDetails.value = Event(details)
                    _newDictionary.value = Event(details)
                }
    }

    private fun observeList() {
        disposables += sortedListRepository.getSortedDictionaryList()
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe {
                    _liveDictionaryList.postValue(it)
                    _dictionaries.value = it
                    isListEmpty.value = it.isEmpty()
                }
    }

    fun clearNewDictionary() {
        _newDictionary.value = Event(null)
    }

    public override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun createDictionaryObject(dictionaryName: String): Dictionary = Dictionary(dictionaryName = dictionaryName,
            dictionaryCreated = Calendar.getInstance().time, dictionaryLastPracticed = null, dictionaryLastResult = null, dictionaryFinishedCount = 0, dictionaryTotalScore = 0)

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
        return quizRepository.resetQuizList(dictionaryId, quizType)
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
