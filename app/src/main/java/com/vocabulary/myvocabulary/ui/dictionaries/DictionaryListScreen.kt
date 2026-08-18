package com.vocabulary.myvocabulary.ui.dictionaries

import android.net.Uri
import android.util.Log
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val context = LocalContext.current
    val viewModel: DictionaryListViewModel = koinViewModel()
    val shareDictViewModel: ShareDictionaryViewModel = koinViewModel()
    val dialogFactory: ComposeDialogFactory = koinInject()
    val newDictionary by viewModel.newDictionary.collectAsState()
    var isSavePressed by rememberSaveable { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    val dictionaryList by viewModel.dictionaries.collectAsState()
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var itemToDelete by rememberSaveable { mutableStateOf<Dictionary?>(null) }
    var itemToEdit by rememberSaveable { mutableStateOf<Dictionary?>(null) }
    var isImport by rememberSaveable { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        shareDictViewModel.importedDictionaryDetailsFlow.collect { event ->
            event.getContentIfNotHandled()?.let { details ->
                shareDictViewModel.parseDataAndCreateWordsCompose(
                    dictionaryId = details.dictionaryId,
                    contentResolver = context.contentResolver
                )
                navigateToWordList(details.dictionaryId, details.dictionaryName)
                Log.d("Import", "Created ${details.dictionaryName}, starting CSV parse...")
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { data ->
            shareDictViewModel.saveCsvData(data)
        }
    }

    LaunchedEffect(isFabExpanded) {
        viewModel.fetchDictionaries()
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
                            isFabExpanded = false
                            showCreateDialog = true
                        },
                        extendedLabelId = R.string.create_fab_label
                    ),
                    FabConfiguration.FabButton(
                        icon = Icons.Default.ImportExport,
                        iconLabelId = R.string.import_fab_label,
                        onClick = {
                            isFabExpanded = false
                            isImport = true
                            filePickerLauncher.launch(Constants.MIME_TYPE)
                        },
                        extendedLabelId = R.string.import_fab_label
                    )
                )
            )
        )
    }

    LaunchedEffect(newDictionary) {
        newDictionary.getContentIfNotHandled()?.let { details ->
            if (isSavePressed && details != null) {
                navigateToWordList(details.dictionaryId, details.dictionaryName)
            }
            isSavePressed = false
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
        DictionaryLazyList(
            list = dictionaryList,
            onShowDeleteDialog = { dictionary ->
                itemToDelete = dictionary
            },
            onShowEditDialog = { dictionary ->
                itemToEdit = dictionary
            },
            onDictionaryClick = { dictionary ->
                navigateToWordList(dictionary.dictionaryId, dictionary.dictionaryName)
            },
            onStartQuiz = { dictionaryId ->
                onStartQuiz(dictionaryId)
            }
        )
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (!isLoading && dictionaryList.isEmpty()) {
            Text(
                text = stringResource(R.string.no_dictionaries_found),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    if (showCreateDialog) {
        dialogFactory.BuildCreateDictionaryDialog(
            onDismissRequest = {
                showCreateDialog = false
            },
            onConfirmation = { newTitle ->
                isSavePressed = true
                showCreateDialog = false
                viewModel.insertDictionary(viewModel.createDictionaryObject(newTitle))
            },
            dialogTitle = stringResource(R.string.create_new_dictionary_dialog_title)
        )
    }

    itemToDelete?.let { dictionary ->
        dialogFactory.BuildDeleteDialog(
            onDismissRequest = { itemToDelete = null },
            onConfirmation = {
                viewModel.deleteDictionary(dictionary)
                itemToDelete = null
            },
            dialogTitle = stringResource(R.string.dialog_delete_dictionary_title) + " \"${dictionary.dictionaryName}\" ?",
            message = stringResource(R.string.verify_deletion)
        )
    }

    itemToEdit?.let { item ->
        dialogFactory.BuildRenameDictionaryDialog(
            onDismissRequest = { itemToEdit = null },
            onConfirmation = { newTitle ->
                viewModel.renameDictionary(item.copy(dictionaryName = newTitle))
                itemToEdit = null
            },
            dialogTitle = stringResource(R.string.renaming_dictionary_title) + " \"${item.dictionaryName}\""
        )
    }

    if (isImport) {
        if (!showCreateDialog) {
            dialogFactory.BuildCreateDictionaryDialog(
                onDismissRequest = {
                    isImport = false
                    shareDictViewModel.setIsImport(false)
                },
                onConfirmation = { newTitle ->
                    isImport = false
                    shareDictViewModel.setIsImport(false)
                    shareDictViewModel.createDictionary(
                        Dictionary(
                            dictionaryName = newTitle,
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        )
                    )
                },
                dialogTitle = stringResource(R.string.import_dictionary_dialog_title)
            )
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

