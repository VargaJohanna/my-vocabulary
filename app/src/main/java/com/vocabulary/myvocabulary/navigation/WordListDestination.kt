package com.vocabulary.myvocabulary.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.dictionaries.ShareDictionaryViewModel
import com.vocabulary.myvocabulary.ui.theme.dimens
import com.vocabulary.myvocabulary.ui.words.WordListScreen
import com.vocabulary.myvocabulary.ui.words.WordListViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.Locale.getDefault

@Composable
fun WordListDestination(
    navBackStackEntry: NavBackStackEntry,
    navController: NavHostController,
    onUpdateTitle: (@Composable () -> Unit) -> Unit,
    onUpdateActions: (@Composable RowScope.() -> Unit) -> Unit,
    onUpdateFab: (FabConfiguration) -> Unit,
    onBackClick: (() -> Unit) -> Unit,
    isSearchVisible: Boolean,
    onToggleSearch: (Boolean) -> Unit,
    isSortOpen: Boolean,
    onToggleSort: (Boolean) -> Unit
) {
    val args = navBackStackEntry.toRoute<WordList>()
    val context = LocalContext.current

    val wordListViewModel: WordListViewModel = koinViewModel(parameters = { parametersOf(args.dictionaryId) })
    val shareViewModel: ShareDictionaryViewModel = koinViewModel()
    val wordListState by wordListViewModel.wordList.collectAsState()

    LaunchedEffect(isSearchVisible, args.dictionaryName, isSortOpen) {
        onUpdateTitle {
            Text( text = args.dictionaryName.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    getDefault()
                ) else it.toString()
            },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        }

        onUpdateActions {
            // Search Toggle
            IconButton(onClick = {
                onToggleSearch(!isSearchVisible)
                onToggleSort(false)
            }) {
                Icon(
                    imageVector = if (isSearchVisible) Icons.Default.Clear else Icons.Default.Search,
                    contentDescription = "Toggle Search"
                )
            }
            // Sort Toggle
            IconButton(onClick = {
                onToggleSort(!isSortOpen)
                onToggleSearch(false)
            } ) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort Word List"
                )
            }
            // More Options Menu
            var expanded by remember { mutableStateOf(false) }
            Box{
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.padding(MaterialTheme.dimens.PaddingLarge)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.dropdown_menu_label),
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.dictionary_menu_start_quiz)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Quiz,
                                contentDescription = stringResource(R.string.start_button_label)
                            )
                        },
                        onClick = {
                            onToggleSearch(false)
                            onToggleSort(false)
                            navController.navigate(QuizList(args.dictionaryId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.export_menu_label)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = stringResource(R.string.export_menu_label)
                            )
                        },
                        onClick = {
                            shareViewModel.shareDictionaryCompose(
                                words = wordListState.first,
                                context = context,
                                dictionaryName = args.dictionaryName
                            )
                        }
                    )
                }
            }
        }

        onBackClick {
            when {
                isSearchVisible -> onToggleSearch(false)
                isSortOpen -> onToggleSort(false)else -> navController.popBackStack()
            }
        }

        onUpdateFab(FabConfiguration.Hidden())
    }

    WordListScreen(
        dictionaryId = args.dictionaryId,
        onUpdateFab = { onUpdateFab },
        isSearchVisible = isSearchVisible,
        onToggleSearch = onToggleSearch,
        isSortOpen = isSortOpen,
        onToggleSort = onToggleSort,
    )
}