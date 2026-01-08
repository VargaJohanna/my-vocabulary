package com.vocabulary.myvocabulary.ui.words

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Calendar

class WordDetailsViewModel(
        private val wordId: Long,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val currentWordLive: MutableLiveData<Word> = MutableLiveData()
    private val _currentWord: MutableStateFlow<Word> = MutableStateFlow(Word(0, 0, "", "", 0, 0, 0, Calendar.getInstance().time))
    val currentWord: MutableStateFlow<Word> = _currentWord

    fun fetchWordById(id: Long) {
        disposables += wordRepository.getWordById(id)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { t ->
//                    currentWordLive.postValue(t)
                    _currentWord.value = t
                }
    }

    fun getCurrentWord(): LiveData<Word> = currentWordLive

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}