package com.vocabulary.myvocabulary.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.vocabulary.myvocabulary.Constants
import com.vocabulary.myvocabulary.ui.about.AboutScreen
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListScreen
import com.vocabulary.myvocabulary.ui.home.HomeScreen
import com.vocabulary.myvocabulary.ui.quizzes.QuizListScreen
import com.vocabulary.myvocabulary.ui.quizzes.QuizScreen
import com.vocabulary.myvocabulary.ui.results.ResultScreen
import com.vocabulary.myvocabulary.ui.words.WordListScreen

@Composable
fun MyVocabularyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
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
                }
            )
        }

        composable<WordList> {
            val args = it.toRoute<WordList>()
            WordListScreen(args.dictionaryId, args.dictionaryName)
        }

        composable<About> {
            AboutScreen()
        }

        composable<QuizList> {
            val args = it.toRoute<QuizList>()
            QuizListScreen(
                dictionaryIdFromArgs = args.dictionaryId,
                onStartQuiz = { quizType, dictionaryId, direction, failedOnly ->
                    navController.navigate(Quiz(quizType, dictionaryId, direction, failedOnly))
                }
            )
        }

        composable<Quiz> {
            val args = it.toRoute<Quiz>()
            QuizScreen(
                args.quizType, args.dictionaryId, args.direction, args.failedOnly,
                onQuizFinished = { dictionaryId, direction, quizType ->
                    navController.navigate(Result(dictionaryId, direction, quizType)) {
                        popUpTo<Quiz> { inclusive = true }
                        launchSingleTop = true
                    }
                })
        }

        composable<Result> {
            val args = it.toRoute<Result>()
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
                }
            )
        }
    }
}