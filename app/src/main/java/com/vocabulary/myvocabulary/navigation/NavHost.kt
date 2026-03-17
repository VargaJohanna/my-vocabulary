package com.vocabulary.myvocabulary.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.about.AboutScreen
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListScreen
import com.vocabulary.myvocabulary.ui.home.HomeScreen
import com.vocabulary.myvocabulary.ui.quizzes.QuizListScreen
import com.vocabulary.myvocabulary.ui.quizzes.QuizScreen
import com.vocabulary.myvocabulary.ui.results.ResultScreen

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
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable<Home> {
            LaunchedEffect(Unit) {
                onUpdateTitle { Text(
                    text = stringResource(R.string.app_name),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis) }

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
            HomeScreen()
        }

        composable<DictionaryList> {
            LaunchedEffect(Unit) {
                onUpdateTitle { Text(
                    text = stringResource(R.string.dictionaries_toolbar),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis) }

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

        composable<WordList> { backStackEntry ->
            WordListDestination(
                navBackStackEntry = backStackEntry,
                navController = navController,        onUpdateTitle = onUpdateTitle,
                onUpdateActions = onUpdateActions,
                onUpdateFab = onUpdateFab,
                onBackClick = onBackClick,
                isSearchVisible = isSearchVisible,
                onToggleSearch = onToggleSearch,
                isSortOpen = isSortOpen,
                onToggleSort = onToggleSort
            )
        }

        composable<About> {
            LaunchedEffect(Unit) {
                onUpdateTitle { Text(
                    text = stringResource(R.string.about_appbar),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis) }

                onUpdateActions { }

                onBackClick { navController.popBackStack() }

                onUpdateFab { }
            }
            AboutScreen()
        }

        composable<QuizList> {
            val args = it.toRoute<QuizList>()
            LaunchedEffect(Unit) {
                onUpdateTitle { Text(
                    text = stringResource(R.string.quizzes_toolbar),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis) }

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
                onUpdateTitle { Text(
                    text = stringResource(R.string.quiz_toolbar),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis) }

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
                        popUpTo<QuizList> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onUpdateFab = onUpdateFab,
                onExit = {
                    screenCleanup?.invoke()
                    navController.navigate(QuizList(dictionaryId = null)) {
                        popUpTo<QuizList> { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Result> {
            val args = it.toRoute<Result>()
            LaunchedEffect(Unit) {
                onUpdateTitle { Text(
                    text = stringResource(R.string.result_fragment_title),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis) }

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