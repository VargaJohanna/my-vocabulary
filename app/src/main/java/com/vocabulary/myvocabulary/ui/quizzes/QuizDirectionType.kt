package com.vocabulary.myvocabulary.ui.quizzes

sealed class QuizDirectionType(
        val id: Int
) {
    object AskMeaning : QuizDirectionType(
            id = 0
    )

    object AskExpression : QuizDirectionType(
            id = 1
    )
}

fun Int.toDirectionType():QuizDirectionType {
    return when(this) {
        0 -> QuizDirectionType.AskMeaning
        1 -> QuizDirectionType.AskExpression
        else -> throw IllegalStateException("Unknown Direction: $this")
    }
}

fun QuizDirectionType.toInt() : Int {
    return when (this) {
        QuizDirectionType.AskMeaning -> 0
        QuizDirectionType.AskExpression -> 1
    }
}