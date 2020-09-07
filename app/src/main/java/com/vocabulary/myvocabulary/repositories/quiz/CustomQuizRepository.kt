package com.vocabulary.myvocabulary.repositories.quiz

import io.reactivex.Observable

interface CustomQuizRepository {
    val quizSize: Observable<Int>
    fun setQuizSize(size: Int)
}