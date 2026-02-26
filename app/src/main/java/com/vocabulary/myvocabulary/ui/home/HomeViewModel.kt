package com.vocabulary.myvocabulary.ui.home

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.quotes.QuoteData
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.quotes.QuoteRepository
import com.vocabulary.myvocabulary.repositories.share.ShareDictionaryRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

class HomeViewModel(
    private val rxSchedulers: RxSchedulers,
    private val quoteRepository: QuoteRepository,
    private val shareDictionaryRepository: ShareDictionaryRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val preferences: SharedPreferences
) : ViewModel() {
    private val _liveQuote: MutableLiveData<QuoteData> = MutableLiveData()
    val liveQuote: LiveData<QuoteData> = _liveQuote
    private val disposables = CompositeDisposable()
    private val openedAppCounter: Int = preferences.getInt(COUNTER_KEY, 0)
    private val _lastPracticedDictionary = MutableStateFlow<Dictionary?>(Dictionary(dictionaryId = 0, dictionaryName = "", dictionaryCreated = Calendar.getInstance().time, dictionaryLastPracticed = null, dictionaryLastResult = null, dictionaryFinishedCount = 0, dictionaryTotalScore = 0))
    val lastPracticedDictionary: StateFlow<Dictionary?> = _lastPracticedDictionary


    init {
        observeQuote()
        getLastPracticedDictionary()
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

    fun getLastPracticedDictionary() {
        val dictionaries = dictionaryRepository.allDictionariesFlow.value ?: emptyList()

        val lastPracticed = dictionaries
            .filter {it.dictionaryLastPracticed != null}
            .maxByOrNull { it.dictionaryLastPracticed!!.time }

        _lastPracticedDictionary.value = lastPracticed
    }


    fun getAverageRate(dictionaryId: Long) {


    }



    companion object {
        const val COUNTER_KEY = "COUNTER"
    }
}