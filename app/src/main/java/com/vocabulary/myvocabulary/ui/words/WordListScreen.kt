package com.vocabulary.myvocabulary.ui.words

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.vocabulary.myvocabulary.R
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vocabulary.myvocabulary.navigation.ProvideAppBarTitle
import com.vocabulary.myvocabulary.ui.theme.MyVocabularyTheme
import com.vocabulary.myvocabulary.ui.theme.dimens
import com.vocabulary.myvocabulary.utils.ComposeDialogFactory
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.Calendar

@Composable
fun WordListScreen(
    dictionaryId: Long,
    dictionaryName: String
) {
    val viewModel: WordListViewModel = koinViewModel(
        parameters = { parametersOf(dictionaryId) }
    )
    val dialogFactory: ComposeDialogFactory = koinInject()

    LaunchedEffect(Unit) {
        viewModel.fetchWordList()
    }

    val wordList by viewModel.wordList.collectAsState()

    WordListScreenContent(
        dictionaryName = dictionaryName,
        wordList = wordList.first,
        dialogFactory = dialogFactory,
        onInsertWord = { newWord, newTranslation ->
            viewModel.insertWord(viewModel.createWordObject(newWord, newTranslation))
        },
        onEditWord = { word ->
            viewModel.updateWord(word)
        },
        onDeleteWord = { word ->
            viewModel.deleteWord(word)
        }
    )
}


@Composable
fun WordListScreenContent(
    dictionaryName: String,
    wordList: List<Word>,
    dialogFactory: ComposeDialogFactory,
    onInsertWord: (String, String) -> Unit,
    onEditWord: (Word) -> Unit,
    onDeleteWord: (Word) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    ProvideAppBarTitle { Text(dictionaryName) }
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    var clickedWordToEdit by rememberSaveable { mutableStateOf(Word(0, 0, "", "", 0, 0, 0, Calendar.getInstance().time)) }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FabMenu(onShowCreateDialog = { showCreateDialog = true })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.dimens.PaddingMedium)
            ) {
                Text(
                    text = stringResource(R.string.word_list_expression),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.word_list_meaning),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            WordLazyList(
                list = wordList,
                onClick = { sheetState, clickedWord ->
                    isSheetOpen = sheetState
                    clickedWordToEdit = clickedWord
                    }
            )

            if (isSheetOpen) {
                WordDetailsBottomSheet(
                    clickedWord = clickedWordToEdit,
                    onDismissRequest = { isSheetOpen = it },
                    showEditDialog = { showEditDialog = it },
                    showDelete = { showDeleteDialog = it }
                )
            }
        }
    }

    if (showCreateDialog) {
        dialogFactory.BuildCreateWordDialog(
            dialogTitle = stringResource(R.string.create_new_word_dialog_title),
            onDismissRequest = {
                showCreateDialog = false
            },
            onConfirmation = { newWord, newTranslation ->
                onInsertWord(newWord, newTranslation)
                showCreateDialog = false
            },
            onAddMore = { newWord, newTranslation ->
                onInsertWord(newWord, newTranslation)
            }
        )
    }

    if (showEditDialog) {
        dialogFactory.BuildEditWordDialog(
            onDismissRequest = {
                showEditDialog = false
            },
            onConfirmation = { editedExpression, editedTranslation ->
                onEditWord(clickedWordToEdit.copy(word = editedExpression, translation = editedTranslation))
                showEditDialog = false
                isSheetOpen = false
            },
            expression = clickedWordToEdit.word,
            translation = clickedWordToEdit.translation
        )
    }

    if (showDeleteDialog) {
        dialogFactory.BuildDeleteDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirmation = {
                onDeleteWord(clickedWordToEdit)
                showDeleteDialog = false
                isSheetOpen = false
                             },
            dialogTitle = stringResource(R.string.dialog_delete_word_title) ,
            message = stringResource(R.string.verify_deletion) + "\n\"${clickedWordToEdit.translation} - ${clickedWordToEdit.word}\" ?"
        )
    }
}

@Composable
fun FabMenu(onShowCreateDialog: () -> Unit) {
    FloatingActionButton(
        onClick = { onShowCreateDialog() }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.dictionary_fab_description),
        )
    }
}

@Composable
fun WordLazyList(
    list: List<Word>,
    onClick: (isSheetOpen: Boolean, clickedWord: Word) -> Unit
) {
    LazyColumn(
        Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(MaterialTheme.dimens.PaddingMedium)
    ) {
        items(list) { item ->
            WordCard(
                modifier = Modifier,
                wordItem = item,
                onClick = { sheetState ->
                    onClick(sheetState, item)
                }
            )
            if (list.last() == item) {
                Spacer(modifier = Modifier.padding(MaterialTheme.dimens.PaddingMedium))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = String.format(stringResource(R.string.number_of_words), list.size),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        }

    }

}

@Composable
fun WordCard(
    modifier: Modifier,
    wordItem: Word,
    onClick: (isSheetOpen: Boolean) -> Unit
) {
    Card(
        onClick = { onClick(true) },
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.dimens.PaddingMedium),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.dimens.CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    modifier = Modifier.padding(MaterialTheme.dimens.PaddingLarge),
                    text = wordItem.word,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            VerticalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(MaterialTheme.dimens.PaddingMedium)
            )
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    modifier = Modifier.padding(MaterialTheme.dimens.PaddingLarge),
                    text = wordItem.translation.ifEmpty { stringResource(R.string.word_hint) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (wordItem.translation.isNotBlank()) Color.Black else Color.Gray
                )
            }
        }
    }
}

@Preview
@Composable
fun WordListScreenPreview() {
    // Some fake data for the preview
    val previewWords = listOf(
        Word(1, 1, "new", "novus", 0, 0, 0, Calendar.getInstance().time),
        Word(2, 1, "body", "corpus", 0, 0, 0, Calendar.getInstance().time),
        Word(3, 1, "day", "diem", 0, 0, 0, Calendar.getInstance().time),
    )

    // Only call the stateless composable with the fake data.
    // Wrap it in your app's theme for consistent styling.
    MyVocabularyTheme {
        WordListScreenContent(
            dictionaryName = "Test Dictionary",
            wordList = previewWords,
            dialogFactory = ComposeDialogFactory(),
            onInsertWord = { _, _ -> /* Do nothing in preview */ },
            onDeleteWord = {_ -> },
            onEditWord = { _ -> }
        )
    }
}
