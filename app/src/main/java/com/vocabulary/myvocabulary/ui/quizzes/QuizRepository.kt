package com.vocabulary.myvocabulary.ui.quizzes

import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import io.reactivex.Single

interface QuizRepository {
    val quizList: Observable<List<Word>>
    fun updateQuizList(list: List<Word>)
    fun resetQuizList(dictionaryId: Long, quizType: QuizTypes): Single<List<Word>>
}