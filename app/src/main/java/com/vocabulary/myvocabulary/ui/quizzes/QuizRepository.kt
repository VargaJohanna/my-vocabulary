package com.vocabulary.myvocabulary.ui.quizzes

import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

interface QuizRepository {
    val _quizList: BehaviorSubject<List<Word>>
    val quizList: Observable<List<Word>>
    fun resetFullQuizList(dictionaryId: Long)
    fun resetQuickQuizList(dictionaryId: Long)
    fun resetWeakestFive(dictionaryId: Long)
}