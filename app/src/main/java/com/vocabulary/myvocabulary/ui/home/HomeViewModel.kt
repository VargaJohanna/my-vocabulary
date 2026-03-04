package com.vocabulary.myvocabulary.ui.home

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.quotes.QuoteData
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.quotes.QuoteRepository
import com.vocabulary.myvocabulary.repositories.share.ShareDictionaryRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val rxSchedulers: RxSchedulers,
    private val quoteRepository: QuoteRepository,
    private val shareDictionaryRepository: ShareDictionaryRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val preferences: SharedPreferences,
    private val wordRepository: WordRepository
) : ViewModel() {
    private val _liveQuote: MutableLiveData<QuoteData> = MutableLiveData()
    val liveQuote: LiveData<QuoteData> = _liveQuote
    private val disposables = CompositeDisposable()
    private val openedAppCounter: Int = preferences.getInt(COUNTER_KEY, 0)
    private val _lastPracticedDictionary = MutableStateFlow<Dictionary?>(
        Dictionary(
            dictionaryId = 0,
            dictionaryName = "",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null,
            dictionaryLastResult = null,
            dictionaryFinishedCount = 0,
            dictionaryTotalScore = 0
        )
    )
    val lastPracticedDictionary: StateFlow<Dictionary?> = _lastPracticedDictionary
    private val _mostPracticedDictionary = MutableStateFlow<Dictionary?>(
        Dictionary(
            dictionaryId = 0,
            dictionaryName = "",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null,
            dictionaryLastResult = null,
            dictionaryFinishedCount = 0,
            dictionaryTotalScore = 0
        )
    )
    val mostPracticedDictionary: StateFlow<Dictionary?> = _mostPracticedDictionary

    private val _leastPracticedDictionary = MutableStateFlow<Dictionary?>(
        Dictionary(
            dictionaryId = 0,
            dictionaryName = "",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null,
            dictionaryLastResult = null,
            dictionaryFinishedCount = 0,
            dictionaryTotalScore = 0
        )
    )
    val leastPracticedDictionary: StateFlow<Dictionary?> = _leastPracticedDictionary
    private val _memoriseList = MutableStateFlow<List<Word>>(emptyList())
    val memoriseList: StateFlow<List<Word>> = _memoriseList
    private val _numOfDictionaries = MutableStateFlow(0)
    val numOfDictionaries: StateFlow<Int> = _numOfDictionaries
    private val _isLoadingWords = MutableStateFlow(false)
    val isLoadingWords: StateFlow<Boolean> = _isLoadingWords.asStateFlow()



    init {
        observeQuote()
        getDictionaryStats()
        getListOfWords()
    }

    private fun observeQuote() {
        disposables += quoteRepository.getQuote()
            .subscribeOn(rxSchedulers.io())
            .observeOn(rxSchedulers.main())
            .subscribe(
                { _liveQuote.postValue(it) },
                {
                    Log.d("QUOTE_ERROR", it.message ?: "Unknown error")
                    _liveQuote.postValue(QuoteData.EMPTY)
                }
            )
    }

    fun saveCsvData(csv: Uri) {
        shareDictionaryRepository.storeCsvData(csv)
    }

    fun setIsImport(isImport: Boolean) {
        shareDictionaryRepository.setIsImport(isImport)
    }

    fun openedAppCount() {
        preferences.edit().apply {
            putInt(COUNTER_KEY, openedAppCounter + 1)
            apply()
        }
    }

    private fun getDictionaryStats() {
        disposables += dictionaryRepository.allDictionaries
            .subscribeOn(rxSchedulers.io())
            .observeOn(rxSchedulers.main())
            .subscribe(
                { list ->
                    _numOfDictionaries.value = list.size
                    _lastPracticedDictionary.value = list
                        .filter { it.dictionaryLastPracticed != null }
                        .maxByOrNull { it.dictionaryLastPracticed!!.time }

                    _mostPracticedDictionary.value = list
                        .maxByOrNull { it.dictionaryFinishedCount }

                    _leastPracticedDictionary.value = list
                        .minByOrNull { it.dictionaryFinishedCount }
                }
            )
    }

    private fun getListOfWords() {
        viewModelScope.launch {
            leastPracticedDictionary
                .collect { dict ->
                    if (dict != null && dict.dictionaryId != 0L) {
                        fetchWordsForDictionary(dict.dictionaryId)
                    }
                }
        }
    }

    fun refreshMemoriseList() {
        _isLoadingWords.value = true
        getListOfWords()
    }
    private fun fetchWordsForDictionary(dictionaryId: Long) {
        disposables += wordRepository.getObservableWordList(dictionaryId)
            .subscribeOn(rxSchedulers.io())
            .observeOn(rxSchedulers.main())
            .subscribe(
                { list ->
                    if (list.isNotEmpty()) {
                        _memoriseList.value = list
                            .sortedByDescending { it.beenAsked }
                            .filter { !it.lastResult }
                            .let { sortedList ->
                                sortedList.take(minOf(sortedList.size, 10))
                            }
                            .shuffled()
                            .take(minOf(list.size, 3))
                    } else {
                        _memoriseList.value = emptyList()
                    }
                    _isLoadingWords.value = false
                },
                { error ->
                    Log.e("HOME_VM", "Error fetching words: ${error.message}")
                }
            )
    }

    companion object {
        const val COUNTER_KEY = "COUNTER"
    }
}