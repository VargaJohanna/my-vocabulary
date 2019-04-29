package com.vocabulary.myvocabulary.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.quotes.QuoteData
import com.vocabulary.myvocabulary.repositories.quotes.QuoteRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import io.reactivex.disposables.CompositeDisposable

class HomeViewModel(
        private val rxSchedulers: RxSchedulers,
        private val quoteRepository: QuoteRepository
) : ViewModel() {
    private val _liveQuote: MutableLiveData<QuoteData> = MutableLiveData()
    val liveQuote: LiveData<QuoteData> = _liveQuote
    private val disposables = CompositeDisposable()

    init {
        observeQuote()
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
}