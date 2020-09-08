package com.vocabulary.myvocabulary.repositories.quiz

import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Completable
import io.reactivex.Observable

interface QuizRepository {
    val quizList: Observable<List<Word>>
    fun updateQuizList(list: List<Word>)
    fun resetQuizList(dictionaryId: Long, quizType: QuizTypes, quizSize: Int? = 0): Completable
}