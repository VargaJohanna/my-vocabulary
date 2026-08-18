package com.vocabulary.myvocabulary.repositories.quiz

import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.ui.words.Word
import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    val quizList: Flow<List<Word>>
    fun updateQuizList(list: List<Word>)
//    fun resetQuizList(dictionaryId: Long, quizType: QuizTypes): Completable
    suspend fun setQuizList(dictionaryId: Long, quizType: QuizTypes)
}