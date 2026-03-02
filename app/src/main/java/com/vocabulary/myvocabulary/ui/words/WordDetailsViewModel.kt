package com.vocabulary.myvocabulary.ui.words

import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Calendar

class WordDetailsViewModel(
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val _currentWord: MutableStateFlow<Word> = MutableStateFlow(Word(0, 0, "", "", 0, 0, 0, Calendar.getInstance().time))

    fun fetchWordById(id: Long) {
        disposables += wordRepository.getWordById(id)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { t ->
                    _currentWord.value = t
                }
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}