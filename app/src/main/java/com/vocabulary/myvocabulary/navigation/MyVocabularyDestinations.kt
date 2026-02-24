package com.vocabulary.myvocabulary.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.ui.graphics.vector.ImageVector
import com.vocabulary.myvocabulary.R
import kotlinx.serialization.Serializable

/**
 * Contract for information needed on every MyVocabulary navigation destination
 */
enum class MyVocabularyDestinations(
    val route: Any,
    val icon: ImageVector,
    @StringRes val label: Int,
    val contentDescription: String? = null
) {
    HOME(
        route = Home, // Assuming Home is a serializable object/class
        icon = Icons.Default.Home,
        label = R.string.home_label,
        contentDescription = "Navigate to Home"
    ),
    DICTIONARIES(
        route = DictionaryList, // Use the serializable object
        icon = Icons.AutoMirrored.Filled.MenuBook,
        label = R.string.library_button,
        contentDescription = "Navigate to Dictionaries"
    ),
    QUIZZES(
        route = QuizList(null),
        icon = Icons.Filled.Quiz,
        label = R.string.quizzes_button,
        contentDescription = "Navigate to Quizzes"
    )
}

/**
 * MyVocabulary app navigation destinations
 */
@Serializable
object Home

@Serializable
object DictionaryList

@Serializable
data class WordList(
    val dictionaryId: Long,
    val dictionaryName: String
)

@Serializable
object About

@Serializable
data class QuizList(
    val dictionaryId: Long?,
)

@Serializable
data class Quiz(
    val quizType: Int,
    val dictionaryId: Long,
    val direction: Int,
    val failedOnly: Boolean
)

@Serializable
data class Result(
    val dictionaryId: Long,
    val direction: Int,
    val quizType: Int,
)