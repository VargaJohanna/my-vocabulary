package com.vocabulary.myvocabulary.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vocabulary.myvocabulary.ui.about.AboutScreen
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListScreen
import com.vocabulary.myvocabulary.ui.home.HomeScreen

@Composable
fun MyVocabularyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Home.route,
        modifier = modifier
    ) {
        composable(route = Home.route) {
            HomeScreen(
                onClickDictionaries = { navController.navigate(DictionaryList.route) }
            )
        }

        composable (route = DictionaryList.route){
            DictionaryListScreen()
        }

        composable (route = About.route){
            AboutScreen()
        }
    }

}