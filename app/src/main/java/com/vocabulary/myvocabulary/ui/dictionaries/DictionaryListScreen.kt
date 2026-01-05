package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.navigation.ProvideAppBarTitle
import com.vocabulary.myvocabulary.utils.ComposeDialogFactory
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import java.util.Calendar

@Composable
fun DictionaryListScreen(
    onClickDictionaryItem: (dictionaryId: Long, dictionaryName: String) -> Unit
) {
    val viewModel: DictionaryListViewModel = koinViewModel()
    val dialogFactory: ComposeDialogFactory = koinInject()

    LaunchedEffect(Unit) {
        viewModel.fetchDictionaries()
    }

    val dictionaryList by viewModel.dictionaries.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Dictionary?>(null) }
    var itemToEdit by remember { mutableStateOf<Dictionary?>(null) }

    ProvideAppBarTitle({ Text(stringResource(R.string.dictionaries_toolbar)) })

    Scaffold(
        floatingActionButton = {
            FABMenu(onShowCreateDialog = { showCreateDialog = true })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            DictionaryLazyList(
                list = dictionaryList,
                onShowDeleteDialog = { dictionary ->
                    itemToDelete = dictionary
                },
                onShowEditDialog = { dictionary ->
                    itemToEdit = dictionary
                },
                onDictionaryClick = { dictionary ->
                    onClickDictionaryItem(dictionary.dictionaryId, dictionary.dictionaryName)
                }
            )
        }
    }

    if (showCreateDialog) {
        dialogFactory.BuildCreateDictionaryDialog(
            onDismissRequest = {
                showCreateDialog = false // Handle dismiss
            },
            onConfirmation = { newTitle ->
                showCreateDialog = false // Handle confirmation
                viewModel.insertDictionary(viewModel.createDictionaryObject(newTitle))
                //Also need to navigate to the newly created dictionary screen
            },
            dialogTitle = stringResource(R.string.create_new_dictionary_dialog_title)
        )
    }

    itemToDelete?.let { dictionary ->
        dialogFactory.BuildDeleteDictionaryDialog(
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FABMenu(onShowCreateDialog: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { expanded = it }
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Clear else Icons.Default.Add,
                    contentDescription = stringResource(R.string.dictionary_fab_description),
                    tint = if (expanded) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
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
                    contentDescription = null
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
                    contentDescription = null
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
    onDictionaryClick: (Dictionary) -> Unit
) {
    val padding = 8.dp
    Card(
        onClick = { onDictionaryClick(dictionaryItem) },
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
                modifier = Modifier.padding(28.dp),
                text = dictionaryItem.dictionaryName,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            DictionaryOptionsButton(dictionaryItem, onShowDeleteDialog, onShowEditDialog)

        }
    }
}

@Composable
fun DictionaryOptionsButton(
    dictionaryItem: Dictionary,
    onShowDeleteDialog: (Dictionary) -> Unit,
    onShowEditDialog: (Dictionary) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = !expanded }, modifier = Modifier.padding(16.dp)) {
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
                onClick = { /* Do something... */ }
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
    onDictionaryClick: (Dictionary) -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(list) { item ->
            DictionaryItemView(
                dictionaryItem = item,
                modifier = Modifier,
                onShowDeleteDialog = onShowDeleteDialog,
                onShowEditDialog = onShowEditDialog,
                onDictionaryClick = onDictionaryClick
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
    ) {
        FlowColumn(
            modifier = Modifier.fillMaxSize(),

            ) {
            DictionaryLazyList(
                list =
                    listOf(
                        Dictionary(
                            dictionaryId = 1L,
                            dictionaryName = "Test",
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
                onDictionaryClick = {}
            )

        }
    }
}

