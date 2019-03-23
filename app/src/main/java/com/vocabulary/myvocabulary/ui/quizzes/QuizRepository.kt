package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.MutableLiveData
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.disposables.CompositeDisposable

interface QuizRepository {
    val liveWordList: MutableLiveData<List<QuizViewModel.FocusableWord>>
    val updateIcon: MutableLiveData<Boolean>
    var listIsFinished: Boolean
    var focusableWordList: MutableList<QuizViewModel.FocusableWord>
    val disposables: CompositeDisposable
    fun observeFullList(dictionaryId: Long, failedOnly: Boolean)
    fun observeQuickList(dictionaryId: Long, failedOnly: Boolean)
    fun observeWeakestList(dictionaryId: Long, failedOnly: Boolean)
    fun resetList()

}