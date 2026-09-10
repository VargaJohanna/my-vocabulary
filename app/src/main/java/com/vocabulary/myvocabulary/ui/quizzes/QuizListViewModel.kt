package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.repositories.quiz.CustomQuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuizListViewModel (
    private val customQuizRepository: CustomQuizRepository
) : ViewModel() {

    var _activeDialog = MutableStateFlow<QuizListDialog?>(null)
    val activeDialog = _activeDialog.asStateFlow()
    fun addCustomQuizSize(size: Int?) {
        size?.let {
            customQuizRepository.quizSize = size
        }
    }

    fun setDialogState(dialog: QuizListDialog) {
        _activeDialog.value = dialog
    }

    fun clearDialogState() {
        _activeDialog.value = null
    }

}
sealed class QuizListDialog {
    data class InfoDialog(val title: String, val text: String) : QuizListDialog()
    object CustomDialog : QuizListDialog()
    object DirectionDialog : QuizListDialog()
}
