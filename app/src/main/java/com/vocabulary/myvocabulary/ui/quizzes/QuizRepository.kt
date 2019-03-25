package com.vocabulary.myvocabulary.ui.quizzes

import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable

interface QuizRepository {
    val quizList: Observable<List<Word>>
    fun updateQuizList(list: List<Word>)
    fun resetQuizList(dictionaryId: Long, quizType: QuizTypes)
}