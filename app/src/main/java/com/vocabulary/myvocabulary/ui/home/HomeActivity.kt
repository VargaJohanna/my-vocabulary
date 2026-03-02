package com.vocabulary.myvocabulary.ui.home

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.vocabulary.myvocabulary.navigation.Home
import com.vocabulary.myvocabulary.navigation.MyVocabularyNavHost
import com.vocabulary.myvocabulary.navigation.MyVocabularyTopAppBar
import com.vocabulary.myvocabulary.ui.theme.MyVocabularyTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : ComponentActivity() {
    private var importDialog: AlertDialog? = null
    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        manageIntent(intent?.data)
        homeViewModel.openedAppCount()

        setContent {
            MyVocabularyTheme {
                MyVocabularyApp()
            }
        }
    }


    private fun manageIntent(data: Uri?) {
        if (data != null) {
            homeViewModel.saveCsvData(data)
            homeViewModel.setIsImport(true)
        } else {
            homeViewModel.setIsImport(false)
        }
    }

    override fun onDestroy() {
        importDialog?.dismiss()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyVocabularyApp() {
    val navController = rememberNavController()
    var appBarTitle by remember { mutableStateOf<(@Composable () -> Unit)>({}) }
    var appBarActions by remember { mutableStateOf<@Composable RowScope.() -> Unit>({})}
    var appBarBackAction by remember { mutableStateOf<@Composable () -> Unit>({})}
    var screenFab by remember { mutableStateOf<@Composable () -> Unit>({}) }
    var currentBackAction by remember { mutableStateOf<() -> Unit>({ navController.popBackStack() }) }
    var isSearchVisible by rememberSaveable { mutableStateOf(false) }
    var isSortOpen by rememberSaveable { mutableStateOf(false) }
    val startDestination = Home
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MyVocabularyTopAppBar(
                navController = navController,
                scrollBehavior = scrollBehavior,
                title = appBarTitle,
                actions = appBarActions,
                onBackClick = { currentBackAction() }
            )
        },
        floatingActionButton = screenFab,

    ) { innerPadding ->
        MyVocabularyNavHost(
            navController = navController,
            startDestination = startDestination,
            onUpdateActions = { appBarActions = it },
            onUpdateTitle = { appBarTitle = it },
            onUpdateFab = { screenFab = it },
            isSearchVisible = isSearchVisible,
            onToggleSearch = { isSearchVisible = it },
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding(), bottom = 0.dp),
            onBackClick = { newAction ->
                currentBackAction = newAction
            },
            isSortOpen = isSortOpen,
            onToggleSort = { isSortOpen = it },
        )
    }
}

