package com.vocabulary.myvocabulary.ui.quizzes

import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable

interface QuizRepository {
    val fullQuizList: Observable<List<Word>>
    fun resetFullQuizList(dictionaryId: Long)
}