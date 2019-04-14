package com.vocabulary.myvocabulary.ui.quizzes

sealed class QuizTypes {
    object QuickQuiz : QuizTypes()
    object FullQuiz : QuizTypes()
    object WeakestQuiz : QuizTypes()
}

fun Int.toQuizType(): QuizTypes {
    return when(this) {
        0 -> QuizTypes.QuickQuiz
        1 -> QuizTypes.FullQuiz
        2 -> QuizTypes.WeakestQuiz
        else -> throw IllegalStateException("Unknown quiz type: $this")
    }
}

fun QuizTypes.toInt(): Int {
    return when(this) {
        QuizTypes.QuickQuiz -> 0
        QuizTypes.FullQuiz -> 1
        QuizTypes.WeakestQuiz -> 2
    }
}