package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.rx.RxSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class ResultViewModel(
        private val rxSchedulers: RxSchedulers
) : ViewModel() {
    private val disposables = CompositeDisposable()
    var guessedWordMap: MutableMap<Long, String> = emptyMap<Long, String>().toMutableMap()

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }
}