package com.vocabulary.myvocabulary.repositories.quiz

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class CustomQuizRepositoryImpl : CustomQuizRepository {
    val _quizSize : BehaviorSubject<Int> = BehaviorSubject.createDefault(10)
    override val quizSize: Observable<Int> = _quizSize

    override fun setQuizSize(size: Int) {
        _quizSize.onNext(size)
    }
}