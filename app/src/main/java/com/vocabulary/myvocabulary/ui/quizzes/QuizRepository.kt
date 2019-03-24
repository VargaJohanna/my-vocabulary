package com.vocabulary.myvocabulary.ui.quizzes

import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

interface QuizRepository {
    val quizList: Observable<List<Word>>
//    val quickQuizList: Observable<List<Word>>
    var isQuizFinished: Boolean
    fun resetFullQuizList(dictionaryId: Long)
    fun resetQuickQuizList(dictionaryId: Long)
    var currentQuizList: MutableList<Word>
    val _quizList: BehaviorSubject<List<Word>>
}