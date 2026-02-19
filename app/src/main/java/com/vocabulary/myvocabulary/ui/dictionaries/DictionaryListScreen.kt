package com.vocabulary.myvocabulary.ui.dictionaries

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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocabulary.myvocabulary.Constants
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortByDictionaryOptions
import com.vocabulary.myvocabulary.ui.theme.dimens
import com.vocabulary.myvocabulary.utils.ComposeDialogFactory
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import java.util.Calendar

@Composable
fun DictionaryListScreen(
    navigateToWordList: (dictionaryId: Long, dictionaryName: String) -> Unit,
    onUpdateFab: (@Composable () -> Unit) -> Unit,
    onStartQuiz: (dictionaryId: Long) -> Unit,
    isSortOpen: Boolean,
    onToggleSort: (Boolean) -> Unit
) {
    val viewModel: DictionaryListViewModel = koinViewModel()
    val dialogFactory: ComposeDialogFactory = koinInject()
    val newDictionary by viewModel.newDictionary.collectAsState()
    var isSavePressed by rememberSaveable { mutableStateOf(false) }
    //isClickable: Set it to true when navigation is done to prevent ghost clicking
    var isClickable by remember { mutableStateOf(true) }
    //screenEntryTime: save entry time to add 5 ms wait to prevent ghost clicking
    val screenEntryTime = remember { System.currentTimeMillis() }
    var isNavigating by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    val dictionaryList by viewModel.dictionaries.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Dictionary?>(null) }
    var itemToEdit by remember { mutableStateOf<Dictionary?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchDictionaries()
        isClickable = true
        onUpdateFab {
            FABMenu(onShowCreateDialog = { showCreateDialog = true })
        }
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
                val currentTime = System.currentTimeMillis()
                if (!isNavigating && (currentTime - screenEntryTime > Constants.NAV_GHOST_CLICK_THRESHOLD)) {
                    isNavigating = true
                    navigateToWordList(dictionary.dictionaryId, dictionary.dictionaryName)
                }
            },
            isClickable = isClickable,
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
            leadingIcon = { Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(R.string.sort_by_date)) }

        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.sort_by_title)) },
            onClick = {
                onSortByTitle()
                onToggleSort(false)
            },
            leadingIcon = { Icon(
                imageVector = Icons.Default.SortByAlpha,
                contentDescription = stringResource(R.string.sort_by_title)) }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FABMenu(onShowCreateDialog: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { expanded = it },
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Clear else Icons.Default.Add,
                    contentDescription = stringResource(R.string.dictionary_fab_description),
                )
            }
        }
    ) {
        FloatingActionButtonMenuItem(
            onClick = {
                expanded = false
            },
            text = { Text(stringResource(R.string.import_fab_label)) },
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_import_export_24),
                    contentDescription = "Import dictionary"
                )
            }
        )
        FloatingActionButtonMenuItem(
            onClick = {
                onShowCreateDialog()
                expanded = false
            },
            text = { Text(stringResource(R.string.create_fab_label)) },
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.outline_create_new_folder_24),
                    contentDescription = "Create new dictionary icon"
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
    isClickable: Boolean,
    onStartQuiz: (dictionaryId: Long) -> Unit
) {
    val padding = MaterialTheme.dimens.PaddingMedium
    Card(
        onClick = {
            if (isClickable) {
                onDictionaryClick(dictionaryItem)
            }
        },
        modifier
            .fillMaxWidth()
            .padding(padding),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        enabled = isClickable,
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
    isClickable: Boolean,
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
                isClickable = isClickable,
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
                            dictionaryCreated = Calendar.getInstance().time
                        ),
                        Dictionary(
                            dictionaryId = 2L,
                            dictionaryName = "Test2",
                            dictionaryCreated = Calendar.getInstance().time
                        ),
                        Dictionary(
                            dictionaryId = 3L,
                            dictionaryName = "Test3",
                            dictionaryCreated = Calendar.getInstance().time
                        ),
                        Dictionary(
                            dictionaryId = 4L,
                            dictionaryName = "Test4",
                            dictionaryCreated = Calendar.getInstance().time
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time
                        ),
                        Dictionary(
                            dictionaryId = 5L,
                            dictionaryName = "Test5",
                            dictionaryCreated = Calendar.getInstance().time
                        )
                    ),
                onShowDeleteDialog = {},
                onShowEditDialog = {},
                onDictionaryClick = {},
                isClickable = true,
                onStartQuiz = {}
            )

        }
    }
}

