package com.vocabulary.myvocabulary.ui.quizzes

import kotlinx.serialization.Serializable

sealed class QuizTypes {
    object QuickQuiz : QuizTypes()
    object FullQuiz : QuizTypes()
    object WeakestQuiz : QuizTypes()
    object CustomQuiz : QuizTypes()

    companion object {
        fun getQuizTypes(): List<QuizTypes> {
            return QuizTypes::class.sealedSubclasses.mapNotNull { it.objectInstance }
        }
    }
}

fun Int.toQuizType(): QuizTypes {
    return when(this) {
        0 -> QuizTypes.QuickQuiz
        1 -> QuizTypes.FullQuiz
        2 -> QuizTypes.WeakestQuiz
        3 -> QuizTypes.CustomQuiz
        else -> throw IllegalStateException("Unknown quiz type: $this")
    }
}

fun QuizTypes.toInt(): Int {
    return when(this) {
        QuizTypes.QuickQuiz -> 0
        QuizTypes.FullQuiz -> 1
        QuizTypes.WeakestQuiz -> 2
        QuizTypes.CustomQuiz -> 3
    }
}