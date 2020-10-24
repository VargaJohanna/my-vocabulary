package com.vocabulary.myvocabulary.ui.home

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.quotes.QuoteData
import com.vocabulary.myvocabulary.repositories.quotes.QuoteRepository
import com.vocabulary.myvocabulary.repositories.search.SearchRepository
import com.vocabulary.myvocabulary.repositories.share.ShareDictionaryRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import io.reactivex.disposables.CompositeDisposable

class HomeViewModel(
        private val rxSchedulers: RxSchedulers,
        private val quoteRepository: QuoteRepository,
        private val shareDictionaryRepository: ShareDictionaryRepository,
        private val searchRepository: SearchRepository,
        private val preferences: SharedPreferences
) : ViewModel() {
    private val _liveQuote: MutableLiveData<QuoteData> = MutableLiveData()
    val liveQuote: LiveData<QuoteData> = _liveQuote
    private val disposables = CompositeDisposable()
    private val openedAppCounter: Int = preferences.getInt(COUNTER_KEY, 0)

    init {
        observeQuote()
        openedAppCount()
    }

    private fun observeQuote() {
        disposables += quoteRepository.getQuote()
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe(
                        { _liveQuote.postValue(it) },
                        {
                            Log.d("QUOTE_ERROR", it.message)
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

    fun searchBarState(): Boolean {
        return searchRepository.showSearchBar().subscribeOn(rxSchedulers.io()).blockingFirst(false)
    }

    fun setSearchBarState(isOpen: Boolean) {
        searchRepository.saveSearchBarStatus(isOpen)
    }

    private fun openedAppCount() {
        preferences.edit().apply {
            putInt(COUNTER_KEY, openedAppCounter + 1)
            apply()
        }
    }

    companion object {
        const val COUNTER_KEY = "COUNTER"
    }
}