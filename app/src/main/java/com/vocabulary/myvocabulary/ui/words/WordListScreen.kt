package com.vocabulary.myvocabulary.ui.words

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.navigation.FabConfiguration
import com.vocabulary.myvocabulary.repositories.sortBy.SortByOptions
import com.vocabulary.myvocabulary.ui.lottie.NewDictionaryAnimation
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
    onUpdateFab: (FabConfiguration) -> Unit,
    isSearchVisible: Boolean,
    onToggleSearch: (Boolean) -> Unit,
    isSortOpen: Boolean,
    onToggleSort: (Boolean) -> Unit,
) {
    val viewModel: WordListViewModel = koinViewModel(
        parameters = { parametersOf(dictionaryId) }
    )

    val dialogFactory: ComposeDialogFactory = koinInject()
    val wordListUiState by viewModel.wordListUiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.activeDialog.collectAsStateWithLifecycle()
    val isSheetOpen by viewModel.isSheetOpen.collectAsStateWithLifecycle()
    val clickedWord by viewModel.clickedWord.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarErrorMessage = stringResource(R.string.snack_bar_error)

    BackHandler(enabled = isSearchVisible) {
        onToggleSearch(false)
    }

    LaunchedEffect(isSearchVisible) {
        if (!isSearchVisible) {
            viewModel.setSearchedTerm("")
        }
    }

    LaunchedEffect(Unit) {
        onUpdateFab (
            FabConfiguration.FabButton(
                isVisible = true,
                onClick = {
                    viewModel.setActiveDialog(WordListDialog.Create)
                },
                icon = Icons.Default.Add,
                iconLabelId = R.string.dictionary_fab_description,
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = wordListUiState) {
            is WordListUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is WordListUiState.Error -> {
                LaunchedEffect(Unit) {
                    snackBarHostState.showSnackbar(
                        message = snackBarErrorMessage,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            is WordListUiState.Empty -> {
                NewDictionaryAnimation(true)
            }
            is WordListUiState.Success -> {
                NewDictionaryAnimation(false)
                WordListScreenContent(
                    wordList = state.wordList,
                    onSearch = { viewModel.setSearchedTerm(it) },
                    isSearchVisible = isSearchVisible,
                    isSortOpen = isSortOpen,
                    onToggleSort = onToggleSort,
                    sortByDate = {
                        viewModel.setSortBy(viewModel.currentSortByData.copy(
                            sortByOption = SortByOptions.SortByDate,
                            dateDescending = !viewModel.currentSortByData.dateDescending)
                        )
                    },
                    sortByExpression = {
                        viewModel.setSortBy(viewModel.currentSortByData.copy(
                            sortByOption = SortByOptions.SortByWord,
                            wordDescending = !viewModel.currentSortByData.wordDescending)
                        )
                    },
                    sortByTranslation = {
                        viewModel.setSortBy(viewModel.currentSortByData.copy(
                            sortByOption = SortByOptions.SortByTranslation,
                            translationDescending = !viewModel.currentSortByData.translationDescending)
                        )
                    },
                    onWordClick = { word ->
                        viewModel.setClickedWord(word)
                        viewModel.setSheetOpen(true)
                    }
                )
            }
        }
    }

    if (isSheetOpen && clickedWord != null) {
        WordDetailsBottomSheet(
            clickedWord = clickedWord!!,
            onDismissRequest = { viewModel.setSheetOpen(it) },
            showEditDialog = { 
                if (it) viewModel.setActiveDialog(WordListDialog.Edit(clickedWord!!))
            },
            showDelete = { 
                if (it) viewModel.setActiveDialog(WordListDialog.Delete(clickedWord!!))
            }
        )
    }

    dialogState?.let { dialog ->
        when (dialog) {
            is WordListDialog.Create -> {
                dialogFactory.BuildCreateWordDialog(
                    dialogTitle = stringResource(R.string.create_new_word_dialog_title),
                    onDismissRequest = {
                        viewModel.clearActiveDialog()
                    },
                    onConfirmation = { newWord, newTranslation ->
                        viewModel.insertWord(viewModel.createWordObject(newWord, newTranslation))
                        viewModel.clearActiveDialog()
                    },
                    onAddMore = { newWord, newTranslation ->
                        viewModel.insertWord(viewModel.createWordObject(newWord, newTranslation))
                    }
                )
            }
            is WordListDialog.Edit -> {
                dialogFactory.BuildEditWordDialog(
                    onDismissRequest = {
                        viewModel.clearActiveDialog()
                    },
                    onConfirmation = { editedExpression, editedTranslation ->
                        viewModel.updateWord(
                            dialog.word.copy(
                                word = editedExpression,
                                translation = editedTranslation
                            )
                        )
                        viewModel.clearActiveDialog()
                        viewModel.setSheetOpen(false)
                    },
                    expression = dialog.word.word,
                    translation = dialog.word.translation
                )
            }
            is WordListDialog.Delete -> {
                dialogFactory.BuildDeleteDialog(
                    onDismissRequest = { viewModel.clearActiveDialog() },
                    onConfirmation = {
                        viewModel.deleteWord(dialog.word)
                        viewModel.clearActiveDialog()
                        viewModel.setSheetOpen(false)
                    },
                    dialogTitle = stringResource(R.string.dialog_delete_word_title),
                    message = stringResource(R.string.verify_deletion) + "\n\"${dialog.word.word} - ${dialog.word.translation}\" ?"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreenContent(
    wordList: List<Word>,
    isSearchVisible: Boolean,
    onSearch: (String) -> Unit,
    isSortOpen: Boolean,
    onToggleSort: (Boolean) -> Unit,
    sortByDate: () -> Unit,
    sortByExpression: () -> Unit,
    sortByTranslation: () -> Unit,
    onWordClick: (Word) -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(isSearchVisible) {
        if (!isSearchVisible) {
            searchQuery = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        if (isSearchVisible) {
            DockedSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.dimens.PaddingLarge),
                expanded = false,
                onExpandedChange = { },
                content = {},
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                            onSearch(it)
                        },
                        onSearch = {
                            onSearch(searchQuery)
                        },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text(stringResource(R.string.search_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        searchQuery = ""
                                        onSearch("")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.clear_search)
                                    )
                                }
                            }
                        }
                    )
                }
            )
        }
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopEnd)
                    .padding(horizontal = MaterialTheme.dimens.PaddingLarge)
            ) {
                SortMenu(
                    onSortByDate = sortByDate,
                    onSortByExpression = sortByExpression,
                    onSortByTranslation = sortByTranslation,
                    isSortOpen = isSortOpen,
                    onToggleSort = onToggleSort
                )
            }
        }
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
            onClick = { clickedWord ->
                onWordClick(clickedWord)
            }
        )
    }
}

@Composable
fun SortMenu(
    onSortByDate: () -> Unit,
    onSortByExpression: () -> Unit,
    onSortByTranslation: () -> Unit,
    isSortOpen: Boolean,
    onToggleSort: (Boolean) -> Unit
) {
    DropdownMenu(
        expanded = isSortOpen,
        onDismissRequest = { onToggleSort(false) }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.sort_by_date)) },
            onClick = {
                onSortByDate()
                onToggleSort(false)
            },
            leadingIcon = { Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(R.string.sort_by_date)) }

        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.sort_by_expression)) },
            onClick = {
                onSortByExpression()
                onToggleSort(false)
            },
            leadingIcon = { Icon(
                imageVector = Icons.Default.SortByAlpha,
                contentDescription = stringResource(R.string.sort_by_expression)) }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.sort_by_translation)) },
            onClick = {
                onSortByTranslation()
                onToggleSort(false)
            },
            leadingIcon = { Icon(
                imageVector = Icons.Default.SortByAlpha,
                contentDescription = stringResource(R.string.sort_by_translation)) }
        )
    }
}

@Composable
fun WordLazyList(
    list: List<Word>,
    onClick: (clickedWord: Word) -> Unit
) {
    val state = rememberLazyListState()

    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(bottom = MaterialTheme.dimens.PaddingExtraLarge),
        contentPadding = PaddingValues(MaterialTheme.dimens.PaddingMedium),
        state = state
    ) {
        items(list) { item ->
            WordCard(
                modifier = Modifier,
                wordItem = item,
                onClick = { 
                    onClick(item)
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
    onClick: () -> Unit
) {
    Card(
        onClick = { onClick() },
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
                )
            }
        }
    }
}

@Preview
@Composable
fun WordListScreenPreview() {
    val previewWords = listOf(
        Word(1, 1, "new", "novus", 0, 0, 0, Calendar.getInstance().time),
        Word(2, 1, "body", "corpus", 0, 0, 0, Calendar.getInstance().time),
        Word(3, 1, "day", "diem", 0, 0, 0, Calendar.getInstance().time),
    )

    MyVocabularyTheme {
        WordListScreenContent(
            wordList = previewWords,
            onSearch = {},
            isSearchVisible = false,
            isSortOpen = true,
            onToggleSort = {},
            sortByDate = {},
            sortByExpression = {},
            sortByTranslation = {},
            onWordClick = {}
        )
    }
}
