package com.vocabulary.myvocabulary.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.about.AboutScreen
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListScreen
import com.vocabulary.myvocabulary.ui.dictionaries.ShareDictionaryViewModel
import com.vocabulary.myvocabulary.ui.home.HomeScreen
import com.vocabulary.myvocabulary.ui.quizzes.QuizListScreen
import com.vocabulary.myvocabulary.ui.quizzes.QuizScreen
import com.vocabulary.myvocabulary.ui.results.ResultScreen
import com.vocabulary.myvocabulary.ui.theme.dimens
import com.vocabulary.myvocabulary.ui.words.WordListScreen
import com.vocabulary.myvocabulary.ui.words.WordListViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.Locale.getDefault

@Composable
fun MyVocabularyNavHost(
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier,
    onUpdateActions: (@Composable RowScope.() -> Unit) -> Unit,
    onUpdateTitle: (@Composable () -> Unit) -> Unit,
    onUpdateFab: (@Composable () -> Unit) -> Unit,
    onBackClick: (() -> Unit) -> Unit,
    onToggleSearch: (Boolean) -> Unit,
    isSearchVisible: Boolean,
    onToggleSort: (Boolean) -> Unit,
    isSortOpen: Boolean,
    onExport: () -> Unit
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable<Home> {
            LaunchedEffect(Unit) {
                onUpdateTitle { Text(stringResource(R.string.app_name)) }

                onUpdateActions {
                    IconButton(onClick = {
                        navController.navigate(About) {
                            launchSingleTop = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.home_info)
                        )
                    }
                }

                onUpdateFab { }

            }

            HomeScreen(
                navController = navController
            )
        }

        composable<DictionaryList> {
            LaunchedEffect(Unit) {
                onUpdateTitle {
                    Text(stringResource(R.string.dictionaries_toolbar))
                }

                onBackClick {
                    if(isSortOpen) {
                        onToggleSort(false)
                    } else {
                        navController.popBackStack()
                    }
                }
            }

            LaunchedEffect(isSortOpen) {
                onUpdateActions {
                    IconButton(onClick = { onToggleSort(!isSortOpen) } ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort Dictionaries"
                        )
                    }
                }
            }

            DictionaryListScreen(
                navigateToWordList = { dictionaryId, dictionaryName ->
                    navController.navigate(WordList(dictionaryId, dictionaryName)) {
                        launchSingleTop = true
                    }
                },
                onStartQuiz = { dictionaryId ->
                    navController.navigate(QuizList(dictionaryId)) {
                        launchSingleTop = true
                    }
                },
                onUpdateFab = onUpdateFab,
                isSortOpen = isSortOpen,
                onToggleSort = onToggleSort,
            )
        }

        composable<WordList> {
            val args = it.toRoute<WordList>()

            val wordListViewModel: WordListViewModel = koinViewModel(parameters = { parametersOf(args.dictionaryId) })
            val shareViewModel: ShareDictionaryViewModel = koinViewModel()
            val wordListState by wordListViewModel.wordList.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(isSearchVisible, args.dictionaryName, isSortOpen) {
                onUpdateTitle {
                    Text(args.dictionaryName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            getDefault()
                        ) else it.toString()
                    })
                }

                onUpdateActions {
                    IconButton(onClick = {
                        onToggleSearch(!isSearchVisible)
                        onToggleSort(false)
                    }) {
                        Icon(
                            imageVector = if (isSearchVisible) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = "Toggle Search"
                        )
                    }

                    IconButton(onClick = {
                        onToggleSort(!isSortOpen)
                        onToggleSearch(false)
                    } ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort Word List"
                        )
                    }

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
                                text = { stringResource(R.string.dictionary_menu_start_quiz) },
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
                                text = { stringResource(R.string.export_menu_label) },
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
//                    if (isSearchVisible) {
//                        onToggleSearch(false)
//                    } else {
//                        navController.popBackStack()
//                    }
//
//                    if (isSortOpen) {
//                        onToggleSort(false)
//                    } else {
//                        navController.popBackStack()
//                    }

                    when {
                        isSearchVisible -> onToggleSearch(false)
                        isSortOpen -> onToggleSort(false)else -> navController.popBackStack()
                    }
                }

                onUpdateFab { }
            }

            WordListScreen(
                dictionaryId = args.dictionaryId,
                onUpdateFab = onUpdateFab,
                isSearchVisible = isSearchVisible,
                onToggleSearch = onToggleSearch,
                isSortOpen = isSortOpen,
                onToggleSort = onToggleSort,
            )
        }

        composable<About> {
            LaunchedEffect(Unit) {
                onUpdateTitle { Text(stringResource(R.string.about_appbar)) }

                onUpdateActions { }

                onBackClick { navController.popBackStack() }

                onUpdateFab { }
            }
            AboutScreen()
        }

        composable<QuizList> {
            val args = it.toRoute<QuizList>()
            LaunchedEffect(Unit) {
                onUpdateTitle { Text(stringResource(R.string.quizzes_toolbar)) }

                onUpdateActions { }

                onBackClick { navController.popBackStack() }

                onUpdateFab { }
            }
            QuizListScreen(
                dictionaryIdFromArgs = args.dictionaryId,
                onStartQuiz = { quizType, dictionaryId, direction, failedOnly ->
                    navController.navigate(Quiz(quizType, dictionaryId, direction, failedOnly))
                }
            )
        }

        composable<Quiz> {
            val args = it.toRoute<Quiz>()
            var screenCleanup: (() -> Unit)? = null

            LaunchedEffect(Unit) {
                onUpdateTitle { Text(stringResource(R.string.quiz_toolbar)) }

                onUpdateActions { }
            }

            QuizScreen(
                args.quizType, args.dictionaryId, args.direction, args.failedOnly,
                onRegisterExitLogic = { cleanup ->
                    screenCleanup = cleanup
                    onBackClick {
                        screenCleanup.invoke() // Run ViewModel cleanup
                        navController.navigate(QuizList(dictionaryId = null)) {
                            popUpTo<QuizList> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onQuizFinished = { dictionaryId, direction, quizType ->
                    navController.navigate(Result(dictionaryId, direction, quizType)) {
                        popUpTo<Quiz> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onUpdateFab = onUpdateFab,
                onExit = {
                    screenCleanup?.invoke()
                    navController.navigate(QuizList(dictionaryId = null)) {
                        popUpTo<QuizList> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Result> {
            val args = it.toRoute<Result>()
            LaunchedEffect(Unit) {
                onUpdateTitle { Text(stringResource(R.string.result_fragment_title)) }

                onUpdateActions { }

                onBackClick {
                    navController.navigate(Home)
                }
            }
            ResultScreen(
                args.dictionaryId, args.direction, args.quizType,
                onRestartQuiz = { quizType, dictionaryId, direction, failedOnly ->
                    navController.navigate(
                        Quiz(quizType, dictionaryId, direction, failedOnly)
                    ) {
                        popUpTo<Quiz> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onExit = {
                    navController.navigate(Home) {
                        popUpTo<Home> { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onUpdateFab = onUpdateFab
            )
        }
    }
}