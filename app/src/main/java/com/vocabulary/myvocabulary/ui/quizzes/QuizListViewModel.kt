package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.repositories.quiz.CustomQuizRepository

class QuizListViewModel (
    private val customQuizRepository: CustomQuizRepository
) : ViewModel() {

    fun addCustomQuizSize(size: Int?) {
        size?.let {
            customQuizRepository.quizSize = size
        }
    }

}