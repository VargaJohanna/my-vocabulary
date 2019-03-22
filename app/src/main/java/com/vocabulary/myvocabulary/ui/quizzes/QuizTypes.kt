package com.vocabulary.myvocabulary.ui.quizzes

sealed class QuizTypes {
    object QuickQuiz : QuizTypes()
    object FullQuiz : QuizTypes()
    object WeakestTenQuiz : QuizTypes()
}

fun Int.toQuizType(): QuizTypes {
    return when(this) {
        0 -> QuizTypes.QuickQuiz
        1 -> QuizTypes.FullQuiz
        2 -> QuizTypes.WeakestTenQuiz
        else -> throw IllegalStateException("Unknown quiz type: $this")
    }
}

fun QuizTypes.toInt(): Int {
    return when(this) {
        QuizTypes.QuickQuiz -> 0
        QuizTypes.FullQuiz -> 1
        QuizTypes.WeakestTenQuiz -> 2
    }
}

