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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.navigation.ProvideAppBarTitle
import com.vocabulary.myvocabulary.ui.theme.MyVocabularyTheme
import com.vocabulary.myvocabulary.ui.theme.dimens
import com.vocabulary.myvocabulary.utils.ComposeDialogFactory
import org.koin.compose.koinInject

@Composable
fun QuizListScreen(
    onStartQuiz: (quizType: Int, dictionaryId: Long, direction: Int, failedOnly: Boolean) -> Unit
) {

    val dialogFactory: ComposeDialogFactory = koinInject()
    val list = QuizTypes.getQuizTypes()
    val quizListViewModel: QuizListViewModel = koinInject()

    QuizListContent(
        list = list,
        dialogFactory = dialogFactory,
        onStartQuiz = { quizType, dictionaryId, direction, failedOnly ->

            onStartQuiz(quizType, dictionaryId, direction, failedOnly)
        },
        onCustomSelected = { quizListViewModel.addCustomQuizSize(size = it) }
    )
}

@Composable
fun QuizListContent(
    list: List<QuizTypes>,
    dialogFactory: ComposeDialogFactory,
    onStartQuiz: (quizType: Int, dictionaryId: Long, direction: Int, failedOnly: Boolean) -> Unit,
    onCustomSelected: (size: Int) -> Unit
) {

    ProvideAppBarTitle { Text(stringResource(R.string.quiz_toolbar)) }
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }
    var showCustomDialog by rememberSaveable { mutableStateOf(false) }
    var dialogTitle by rememberSaveable { mutableStateOf("") }
    var dialogText by rememberSaveable { mutableStateOf("") }
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showDirectionDialog by rememberSaveable { mutableStateOf(false) }
    var selectedQuiz: Int by rememberSaveable { mutableStateOf(0) }
    var selectedDictionaryId: Long by rememberSaveable { mutableStateOf(0L) }
    var selectedDirection by rememberSaveable { mutableStateOf(0) }

    val sortedList = remember(list) {
        list.sortedBy { it.toInt() }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(MaterialTheme.dimens.PaddingMedium)
        ) {
            items(sortedList) { item ->
                QuizCard(
                    quizType = item,
                    onInfoClick = { title, info ->
                        dialogTitle = title
                        dialogText = info
                        showInfoDialog = true
                    },
                    onTypeClick = {
                        selectedQuiz = item.toInt()
                        isSheetOpen = true
                    },
                    onCustomClick = { showCustomDialog = true }
                )
            }
        }
        if (isSheetOpen) {
            DictionaryPickerBottomSheet(
                onDismissRequestBottomSheet = { isSheetOpen = it },
                selectedDictionaryId = { selectedDictionaryId = it },
                showDialog = { showDirectionDialog = it }
            )
        }

    }

    if (showInfoDialog) {
        dialogFactory.BuildInfoDialog(
            onDismissRequest = { showInfoDialog = false },
            dialogTitle = dialogTitle,
            dialogText = dialogText
        )
    }

    if (showDirectionDialog) {
        dialogFactory.BuildChooseDirectionDialog(
            onDismissRequest = {
                showDirectionDialog = false
            },
            onConfirmation = { direction ->
                selectedDirection = direction
                onStartQuiz(selectedQuiz, selectedDictionaryId, selectedDirection, false)
                showDirectionDialog = false
                isSheetOpen = false
            }
        )
    }

    if (showCustomDialog) {
        dialogFactory.BuildCustomQuizSizeDialog(
            onDismissRequest = {
                showCustomDialog = false
                isSheetOpen = false
            },
            onConfirmation = { size ->
                onCustomSelected(size)
                selectedQuiz = QuizTypes.CustomQuiz.toInt()
                isSheetOpen = true
                showCustomDialog = false
            }
        )
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
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.dimens.CardElevationSmall)
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
            onCustomSelected = {}
        )
    }
}