package com.vocabulary.myvocabulary.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.toUpperCase
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.vocabulary.myvocabulary.Constants
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.about.AboutScreen
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListScreen
import com.vocabulary.myvocabulary.ui.home.HomeScreen
import com.vocabulary.myvocabulary.ui.quizzes.QuizListScreen
import com.vocabulary.myvocabulary.ui.quizzes.QuizScreen
import com.vocabulary.myvocabulary.ui.results.ResultScreen
import com.vocabulary.myvocabulary.ui.words.WordListScreen
import java.util.Locale.getDefault

@Composable
fun MyVocabularyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onUpdateActions: (@Composable RowScope.() -> Unit) -> Unit,
    onUpdateTitle: (@Composable () -> Unit) -> Unit,
    onUpdateFab: (@Composable () -> Unit) -> Unit,
    onBackClick: (() -> Unit) -> Unit

) {

    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(Constants.CLICK_DEBOUNCE_TIME.toInt())) + slideInHorizontally(
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(Constants.CLICK_DEBOUNCE_TIME.toInt())) + slideOutHorizontally(
                animationSpec = tween(300)
            )
        }
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
                onClickDictionaries = {
                    navController.navigate(
                        DictionaryList
                    ) {
                        launchSingleTop = true
                    }
                },
                onClickQuiz = {
                    navController.navigate(QuizList(null)) {
                        launchSingleTop = true
                    }
                }

            )
        }

        composable<DictionaryList> {
            LaunchedEffect(Unit) {
                onUpdateTitle {
                    Text(stringResource(R.string.dictionaries_toolbar))
                }

                onUpdateActions {
                    IconButton(onClick = { /* Handle Menu Click */ }) {
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
                onUpdateFab = onUpdateFab
            )
        }

        composable<WordList> {
            val args = it.toRoute<WordList>()

            LaunchedEffect(Unit) {
                onUpdateTitle {
                    Text(args.dictionaryName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            getDefault()
                        ) else it.toString()
                    })
                }

                onUpdateActions {
                    IconButton(onClick = { /* Handle Menu Click */ }) {
                        Icon(
                            Icons.Default.Quiz,
                            contentDescription = stringResource(R.string.start_button_label)
                        )
                    }
                }

                onBackClick { navController.popBackStack() }

                onUpdateFab { }

            }
            WordListScreen(
                dictionaryId = args.dictionaryId,
                onUpdateFab = onUpdateFab
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