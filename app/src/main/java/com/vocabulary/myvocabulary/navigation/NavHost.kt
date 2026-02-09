package com.vocabulary.myvocabulary.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
    val localNavController = staticCompositionLocalOf<NavController> {
        error("NavController not provided")
    }

    CompositionLocalProvider(localNavController provides navController) {

        NavHost(
            navController = navController,
            startDestination = Home,
            modifier = modifier
        ) {

            composable<Home> {
                HomeScreen(
                    onClickDictionaries = {
                        navController.navigate(
                            DictionaryList
                        )
                    },
                    onClickQuiz = {
                        navController.navigate(QuizList)
                    }

                )
            }

            composable<DictionaryList> {
                DictionaryListScreen(
                    onClickDictionaryItem = { dictionaryId, dictionaryName ->
                        navController.navigate(WordList(dictionaryId, dictionaryName))
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
                QuizListScreen(
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
                        navController.navigate(Result(dictionaryId, direction, quizType))
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
                            popUpTo<Result> { inclusive = true }
                        }
                    },
                    onExit = {
                        navController.navigate(Home) {
                            popUpTo<Home> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}