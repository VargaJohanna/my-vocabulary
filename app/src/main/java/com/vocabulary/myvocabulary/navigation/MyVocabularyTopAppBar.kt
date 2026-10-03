package com.vocabulary.myvocabulary.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.vocabulary.myvocabulary.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyVocabularyTopAppBar(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    titleOverride: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    onBackClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isTopLevelDestination = currentDestination?.hasRoute(Home::class) == true

    val canNavigateBack = navController.previousBackStackEntry != null && !isTopLevelDestination

    val computedTitleText = when {
        currentDestination?.hasRoute(Home::class) == true -> stringResource(R.string.app_name)
        currentDestination?.hasRoute(DictionaryList::class) == true -> stringResource(R.string.dictionaries_toolbar)
        currentDestination?.hasRoute(QuizList::class) == true -> stringResource(R.string.quizzes_toolbar)
        currentDestination?.hasRoute(About::class) == true -> stringResource(R.string.about_appbar)
        currentDestination?.hasRoute(Quiz::class) == true -> stringResource(R.string.quiz_toolbar)
        currentDestination?.hasRoute(Result::class) == true -> stringResource(R.string.result_fragment_title)
        currentDestination?.hasRoute(WordList::class) == true -> {
            try {
                navBackStackEntry?.toRoute<WordList>()?.dictionaryName ?: ""
            } catch (e: Exception) {
                ""
            }
        }
        else -> ""
    }

    val finalTitle: @Composable () -> Unit = titleOverride ?: {
        if (computedTitleText.isNotEmpty()) {
            Text(
                text = computedTitleText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    TopAppBar(
        title = finalTitle,
        actions = actions,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = {
                    onBackClick.invoke()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_arrow)
                    )
                }
            }
        }
    )
}
