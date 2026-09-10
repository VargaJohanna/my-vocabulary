package com.vocabulary.myvocabulary.ui.dictionaries

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vocabulary.myvocabulary.Constants
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.navigation.FabConfiguration
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortByDictionaryOptions
import com.vocabulary.myvocabulary.ui.theme.dimens
import com.vocabulary.myvocabulary.utils.ComposeDialogFactory
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import java.util.Calendar

@Composable
fun DictionaryListScreen(
    navigateToWordList: (dictionaryId: Long, dictionaryName: String) -> Unit,
    onUpdateFab: (FabConfiguration) -> Unit,
    onStartQuiz: (dictionaryId: Long) -> Unit,
    isSortOpen: Boolean,
    onToggleSort: (Boolean) -> Unit,
) {
    val viewModel: DictionaryListViewModel = koinViewModel()
    val dialogFactory: ComposeDialogFactory = koinInject()
    val dialogState by viewModel.activeDialog.collectAsStateWithLifecycle()
    val createDictionaryEvent by viewModel.createDictionaryEvent.collectAsStateWithLifecycle()
    var isFabExpanded by rememberSaveable { mutableStateOf(false) }
    val libraryUiState by viewModel.libraryUiState.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarErrorMessage = stringResource(R.string.snack_bar_error)
    val snackBarEmptyMessage = stringResource(R.string.no_dictionaries_found)


    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryEvent.NavigateToWordList -> {
                    navigateToWordList(event.dictionaryId, event.dictionaryName)
                }
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { data ->
            viewModel.saveCsvData(data)
        }
    }

    LaunchedEffect(isFabExpanded) {
        onUpdateFab(
            FabConfiguration.FabMenu(
                isVisible = true,
                expanded = isFabExpanded,
                onExpandedChange = { isFabExpanded = it },
                icon = if (isFabExpanded) Icons.Default.Clear else Icons.Default.Add,
                labelId = R.string.dictionary_fab_description,
                items = listOf(
                    FabConfiguration.FabButton(
                        icon = Icons.Outlined.CreateNewFolder,
                        iconLabelId = R.string.create_fab_label,
                        onClick = {
                            viewModel.setActiveDialogState(DictionaryDialog.Create)
                            isFabExpanded = false
                        },
                        extendedLabelId = R.string.create_fab_label
                    ),
                    FabConfiguration.FabButton(
                        icon = Icons.Default.ImportExport,
                        iconLabelId = R.string.import_fab_label,
                        onClick = {
                            isFabExpanded = false
                            viewModel.setIsImport(true)
                            viewModel.setActiveDialogState(DictionaryDialog.Import)
                            filePickerLauncher.launch(Constants.MIME_TYPE)
                        },
                        extendedLabelId = R.string.import_fab_label
                    )
                )
            )
        )
    }

    LaunchedEffect(createDictionaryEvent) {
        createDictionaryEvent?.getContentIfNotHandled()?.let { details ->
            navigateToWordList(details.dictionaryId, details.dictionaryName)
            viewModel.clearNewDictionary()
        }
    }

    val sortByDate = {
        viewModel.setSortBy(
            viewModel.currentSortByData.copy(
                sortByOption = SortByDictionaryOptions.SortByDate,
                dateDescending = !viewModel.currentSortByData.dateDescending
            )
        )
    }

    val sortByTitle = {
        viewModel.setSortBy(
            viewModel.currentSortByData.copy(
                sortByOption = SortByDictionaryOptions.SortByTitle,
                titleDescending = !viewModel.currentSortByData.titleDescending
            )
        )
    }

    dialogState?.let { dialog ->
        isFabExpanded = false
        when (dialog) {
            is DictionaryDialog.Create -> {
                dialogFactory.BuildCreateDictionaryDialog(
                    onDismissRequest = {
                        viewModel.clearActiveDialogState()
                    },
                    onConfirmation = { newTitle ->
                        viewModel.insertDictionary(viewModel.createDictionaryObject(newTitle))
                        viewModel.clearActiveDialogState()
                    },
                    dialogTitle = stringResource(R.string.create_new_dictionary_dialog_title)
                )
            }
            is DictionaryDialog.Delete -> {
                dialogFactory.BuildDeleteDialog(
                    onDismissRequest = {
                        viewModel.clearActiveDialogState()
                    },
                    onConfirmation = {
                        viewModel.deleteDictionary(dialog.dictionary)
                        viewModel.clearActiveDialogState()
                    },
                    dialogTitle = stringResource(R.string.dialog_delete_dictionary_title) + " \"${dialog.dictionary.dictionaryName}\" ?",
                    message = stringResource(R.string.verify_deletion)
                )
            }
            is DictionaryDialog.Edit -> {
                dialogFactory.BuildRenameDictionaryDialog(
                    onDismissRequest = {
                        viewModel.clearActiveDialogState()
                    },
                    onConfirmation = { newTitle ->

                        viewModel.renameDictionary(dialog.dictionary.copy(dictionaryName = newTitle))
                        viewModel.clearActiveDialogState()

                    },
                    dialogTitle = stringResource(R.string.renaming_dictionary_title) + " \"${dialog.dictionary.dictionaryName}\""
                )
            }
            is DictionaryDialog.Import -> {
                dialogFactory.BuildCreateDictionaryDialog(
                    onDismissRequest = {
                        viewModel.clearActiveDialogState()
                        viewModel.setIsImport(false)
                    },
                    onConfirmation = { newTitle ->
                        viewModel.setIsImport(false)
                        viewModel.createImportedDictionary(
                            Dictionary(
                                dictionaryName = newTitle,
                                dictionaryCreated = Calendar.getInstance().time,
                                dictionaryLastPracticed = null,
                                dictionaryLastResult = null,
                                dictionaryFinishedCount = 0,
                                dictionaryTotalScore = 0
                            )
                        )
                        viewModel.clearActiveDialogState()
                    },
                    dialogTitle = stringResource(R.string.import_dictionary_dialog_title)
                )
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopEnd)
                    .padding(horizontal = MaterialTheme.dimens.PaddingLarge)
            ) {
                SortMenu(
                    onSortByDate = sortByDate,
                    onSortByTitle = sortByTitle,
                    isSortOpen = isSortOpen,
                    onToggleSort = { toggle ->
                        onToggleSort(toggle)
                    }
                )
            }
        }
        when (val uiState = libraryUiState) {
            is LibraryUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is LibraryUiState.Error -> {
                LaunchedEffect(Unit) {
                    snackBarHostState.showSnackbar(
                        message = snackBarErrorMessage,
                        duration = SnackbarDuration.Short
                    )
                    //Should go back to Home. Use Event maybe?
                }
            }

            is LibraryUiState.LibraryData -> {
                DictionaryLazyList(
                    list = uiState.dictionaryList,
                    onShowDeleteDialog = { dictionary ->
                        viewModel.setActiveDialogState(DictionaryDialog.Delete(dictionary))
                    },
                    onShowEditDialog = { dictionary ->
                        viewModel.setActiveDialogState(DictionaryDialog.Edit(dictionary))
                    },
                    onDictionaryClick = { dictionary ->
                        navigateToWordList(dictionary.dictionaryId, dictionary.dictionaryName)
                    },
                    onStartQuiz = { dictionaryId ->
                        onStartQuiz(dictionaryId)
                    }
                )
            }

            is LibraryUiState.Empty -> {
                LaunchedEffect(Unit) {
                    snackBarHostState.showSnackbar(
                        message = snackBarEmptyMessage,
                        duration = SnackbarDuration.Short
                    )
                    //Should go back to Home. Use Event maybe?
                }
            }
        }
    }
}

@Composable
fun SortMenu(
    onSortByDate: () -> Unit,
    onSortByTitle: () -> Unit,
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
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.sort_by_date)
                )
            }

        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.sort_by_title)) },
            onClick = {
                onSortByTitle()
                onToggleSort(false)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.SortByAlpha,
                    contentDescription = stringResource(R.string.sort_by_title)
                )
            }
        )
    }
}

@Composable
fun DictionaryItemView(
    dictionaryItem: Dictionary,
    modifier: Modifier = Modifier,
    onShowDeleteDialog: (Dictionary) -> Unit,
    onShowEditDialog: (Dictionary) -> Unit,
    onDictionaryClick: (Dictionary) -> Unit,
    onStartQuiz: (dictionaryId: Long) -> Unit
) {
    val padding = MaterialTheme.dimens.PaddingMedium
    Card(
        onClick = {
            onDictionaryClick(dictionaryItem)
        },
        modifier
            .fillMaxWidth()
            .padding(padding),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.PaddingLarge)
                    .weight(0.7f),
                text = dictionaryItem.dictionaryName,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
            )

            DictionaryOptionsButton(
                dictionaryItem,
                onShowDeleteDialog,
                onShowEditDialog,
                onStartQuiz
            )

        }
    }

}

@Composable
fun DictionaryOptionsButton(
    dictionaryItem: Dictionary,
    onShowDeleteDialog: (Dictionary) -> Unit,
    onShowEditDialog: (Dictionary) -> Unit,
    onStartQuiz: (dictionaryId: Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.padding(MaterialTheme.dimens.PaddingLarge)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.dict_options_description),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.dictionary_menu_start_quiz)) },
                onClick = { onStartQuiz(dictionaryItem.dictionaryId) }
            )

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text(stringResource(R.string.dictionary_menu_rename)) },
                onClick = {
                    onShowEditDialog(dictionaryItem)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.dictionary_menu_delete)) },
                onClick = {
                    onShowDeleteDialog(dictionaryItem)
                    expanded = false
                }
            )
        }
    }

}

@Composable
fun DictionaryLazyList(
    list: List<Dictionary>,
    onShowDeleteDialog: (Dictionary) -> Unit,
    onShowEditDialog: (Dictionary) -> Unit,
    onDictionaryClick: (Dictionary) -> Unit,
    onStartQuiz: (dictionaryId: Long) -> Unit
) {
    val state = rememberLazyListState()

    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.dimens.PaddingMedium)
    ) {
        items(list) { item ->
            DictionaryItemView(
                dictionaryItem = item,
                modifier = Modifier,
                onShowDeleteDialog = onShowDeleteDialog,
                onShowEditDialog = onShowEditDialog,
                onDictionaryClick = onDictionaryClick,
                onStartQuiz = onStartQuiz
            )
        }
    }
}

@Preview
@Composable
fun DictionaryListScreenPreview() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        FlowColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DictionaryLazyList(
                list =
                    listOf(
                        Dictionary(
                            dictionaryId = 1L,
                            dictionaryName = "Test that's very very very long and I want to see the option button",
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        ),
                        Dictionary(
                            dictionaryId = 2L,
                            dictionaryName = "Test2",
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        ),
                        Dictionary(
                            dictionaryId = 3L,
                            dictionaryName = "Test3",
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        ),
                        Dictionary(
                            dictionaryId = 4L,
                            dictionaryName = "Test4",
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        )
                    ),
                onShowDeleteDialog = {},
                onShowEditDialog = {},
                onDictionaryClick = {},
                onStartQuiz = {}
            )

        }
    }
}

