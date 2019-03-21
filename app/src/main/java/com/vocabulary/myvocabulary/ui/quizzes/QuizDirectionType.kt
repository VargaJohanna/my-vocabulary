package com.vocabulary.myvocabulary.ui.quizzes

sealed class QuizDirectionType(
        val id: Int
) {
    object AskWord : QuizDirectionType(
            id = 0
    )

    object AskTranslation : QuizDirectionType(
            id = 1
    )
}

fun Int.toDirectionType():QuizDirectionType {
    return when(this) {
        0 -> QuizDirectionType.AskWord
        1 -> QuizDirectionType.AskTranslation
        else -> throw IllegalStateException("Unknown Direction: $this")
    }
}

fun QuizDirectionType.toInt() : Int {
    return when (this) {
        QuizDirectionType.AskWord -> 0
        QuizDirectionType.AskTranslation -> 1
    }
}