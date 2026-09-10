package com.vocabulary.myvocabulary.ui.quizzes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.theme.MyVocabularyTheme
import com.vocabulary.myvocabulary.ui.theme.dimens
import com.vocabulary.myvocabulary.utils.ComposeDialogFactory
import org.koin.compose.koinInject

@Composable
fun QuizListScreen(
    dictionaryIdFromArgs: Long?,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onStartQuiz: (quizType: Int, dictionaryId: Long, direction: Int, failedOnly: Boolean) -> Unit
) {
    val dialogFactory: ComposeDialogFactory = koinInject()
    val list = QuizTypes.getQuizTypes()
    val quizListViewModel: QuizListViewModel = koinInject()
    val dialogState by quizListViewModel.activeDialog.collectAsStateWithLifecycle()

    QuizListContent(
        list = list,
        dialogFactory = dialogFactory,
        onStartQuiz = { quizType, dictionaryId, direction, failedOnly ->
            onStartQuiz(quizType, dictionaryId, direction, failedOnly)
        },
        dictionaryIdFromArgs = dictionaryIdFromArgs,
        contentPadding = contentPadding,
        quizListViewModel = quizListViewModel,
        dialogState = dialogState
    )
}

@Composable
fun QuizListContent(
    list: List<QuizTypes>,
    dialogFactory: ComposeDialogFactory,
    onStartQuiz: (quizType: Int, dictionaryId: Long, direction: Int, failedOnly: Boolean) -> Unit,
    dictionaryIdFromArgs: Long?,
    contentPadding: PaddingValues,
    quizListViewModel: QuizListViewModel?,
    dialogState: QuizListDialog?
) {
    var isDictionarySheetOpen by rememberSaveable { mutableStateOf(false) }
    var selectedQuiz: Int by rememberSaveable { mutableStateOf(0) }
    var selectedDictionaryId: Long by rememberSaveable { mutableStateOf(0L) }
    var hasDictionaryArg by rememberSaveable { mutableStateOf(false) }
    var argDictionaryId by rememberSaveable { mutableStateOf(0L) }

    LaunchedEffect(dictionaryIdFromArgs) {
        dictionaryIdFromArgs?.let { id ->
            hasDictionaryArg = true
            argDictionaryId = id
        }
    }

    val sortedList = remember(list) {
        list.sortedBy { it.toInt() }
    }

    val viewmodel = quizListViewModel ?: return
        dialogState?.let { dialog ->
            when (dialog) {
                is QuizListDialog.CustomDialog -> {
                    dialogFactory.BuildCustomQuizSizeDialog(
                        onDismissRequest = {
                            viewmodel.clearDialogState()
                        },
                        onConfirmation = { size ->
                            viewmodel.addCustomQuizSize(size)
                            selectedQuiz = QuizTypes.CustomQuiz.toInt()
                            viewmodel.clearDialogState()
                            isDictionarySheetOpen = true
                        }
                    )
                }

                is QuizListDialog.DirectionDialog -> {
                    dialogFactory.BuildChooseDirectionDialog(
                        onDismissRequest = {
                            viewmodel.clearDialogState()
                        },
                        onConfirmation = { direction ->
                            onStartQuiz(selectedQuiz, selectedDictionaryId, direction, false)
                            viewmodel.clearDialogState()
                            isDictionarySheetOpen = false
                        }
                    )
                }

                is QuizListDialog.InfoDialog -> {
                    dialogFactory.BuildInfoDialog(
                        onDismissRequest = { viewmodel.clearDialogState() },
                        dialogTitle = dialog.title,
                        dialogText = dialog.text
                    )
                }
            }
        }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + MaterialTheme.dimens.PaddingSmall,
                start = MaterialTheme.dimens.PaddingMedium,
                end = MaterialTheme.dimens.PaddingMedium,
                bottom = contentPadding.calculateBottomPadding() + MaterialTheme.dimens.PaddingMedium
            )
        ) {
            items(sortedList) { item ->
                QuizCard(
                    quizType = item,
                    onInfoClick = { title, info ->
                        viewmodel.setDialogState(QuizListDialog.InfoDialog(title = title, text = info))
                    },
                    onTypeClick = {
                        selectedQuiz = item.toInt()
                        isDictionarySheetOpen = true
                    },
                    onCustomClick = { viewmodel.setDialogState(QuizListDialog.CustomDialog) }
                )
            }
        }
        if (isDictionarySheetOpen && !hasDictionaryArg) {
            DictionaryPickerBottomSheet(
                onDismissRequestBottomSheet = { isDictionarySheetOpen = it },
                selectedDictionaryId = { selectedDictionaryId = it },
                showDialog = {
                    if (it) viewmodel.setDialogState(QuizListDialog.DirectionDialog)
                    else viewmodel.clearDialogState()
                }
            )
        } else if (isDictionarySheetOpen && hasDictionaryArg) {
            selectedDictionaryId = argDictionaryId
            isDictionarySheetOpen = false
            viewmodel.setDialogState(QuizListDialog.DirectionDialog)
        }
    }
}

@Composable
fun QuizCard(
    quizType: QuizTypes,
    onInfoClick: (title: String, info: String) -> Unit,
    onTypeClick: () -> Unit,
    onCustomClick: () -> Unit
) {
    Card(
        onClick = {
            if (quizType == QuizTypes.CustomQuiz) {
                onCustomClick()
            } else {
                onTypeClick()
            }
        },
        modifier = Modifier
            .padding(MaterialTheme.dimens.PaddingMedium)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.dimens.CardElevationSmall),
    ) {
        val quiz: Pair<String, String> = when (quizType) {
            QuizTypes.QuickQuiz -> Pair(
                stringResource(R.string.quiz_list_quick_one),
                stringResource(R.string.ask_everything_info)
            )

            QuizTypes.FullQuiz -> Pair(
                stringResource(R.string.quiz_list_ask_me_everything),
                stringResource(R.string.quick_list_info)
            )

            QuizTypes.WeakestQuiz -> Pair(
                stringResource(R.string.quiz_list_weaknesses),
                stringResource(R.string.weaknesses_info)
            )

            QuizTypes.CustomQuiz -> Pair(
                stringResource(R.string.quiz_list_custom),
                stringResource(R.string.custom_info)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = quiz.first,
                modifier = Modifier
                    .padding(MaterialTheme.dimens.PaddingExtraLarge)
                    .align(Alignment.Center),
                style = MaterialTheme.typography.titleLarge
            )

            IconButton(
                onClick = { onInfoClick(quiz.first, quiz.second) },
                modifier = Modifier
                    .padding(MaterialTheme.dimens.PaddingSmall)
                    .align(Alignment.TopEnd),
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Quiz Info Button"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuizListScreenPreview() {
    MyVocabularyTheme {
        val previewList = listOf(
            QuizTypes.QuickQuiz,
            QuizTypes.FullQuiz,
            QuizTypes.WeakestQuiz,
            QuizTypes.CustomQuiz
        )
        QuizListContent(
            list = previewList,
            dialogFactory = ComposeDialogFactory(),
            onStartQuiz = { _, _, _, _ -> },
            dictionaryIdFromArgs = null,
            contentPadding = PaddingValues(0.dp),
            quizListViewModel = null,
            dialogState = null
        )
    }
}