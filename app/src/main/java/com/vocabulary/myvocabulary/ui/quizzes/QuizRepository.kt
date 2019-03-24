package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.MutableLiveData
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

interface QuizRepository {
    val fullQuizList: Observable<List<Word>>
    fun getFullQuizList()

    fun startNewQuiz(dictionaryId: Long)
}