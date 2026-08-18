package com.vocabulary.myvocabulary.ui.home

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vocabulary.myvocabulary.navigation.DictionaryList
import com.vocabulary.myvocabulary.navigation.FabConfiguration
import com.vocabulary.myvocabulary.navigation.Home
import com.vocabulary.myvocabulary.navigation.MyVocabularyDestinations
import com.vocabulary.myvocabulary.navigation.MyVocabularyNavHost
import com.vocabulary.myvocabulary.navigation.MyVocabularyTopAppBar
import com.vocabulary.myvocabulary.navigation.QuizList
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyVocabularyApp() {
    val navController = rememberNavController()
    var appBarTitle by remember { mutableStateOf<(@Composable () -> Unit)>({}) }
    var appBarActions by remember { mutableStateOf<@Composable RowScope.() -> Unit>({}) }
    var appBarBackAction by remember { mutableStateOf<@Composable () -> Unit>({}) }
    var screenFab by remember { mutableStateOf<FabConfiguration>(FabConfiguration.Hidden()) }
    var currentBackAction by remember { mutableStateOf<() -> Unit>({ navController.popBackStack() }) }
    var isSearchVisible by rememberSaveable { mutableStateOf(false) }
    var isSortOpen by rememberSaveable { mutableStateOf(false) }
    val startDestination = Home
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val shouldShowBottomBar = currentDestination?.hasRoute(DictionaryList::class) == true ||
            currentDestination?.hasRoute(QuizList::class) == true ||
            currentDestination?.hasRoute(Home::class) == true

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
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    MyVocabularyDestinations.entries.forEach { destination ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.hasRoute(destination.route::class)
                        } == true

                        NavigationBarItem(
                            selected = isSelected,
                            label = { Text(text = stringResource(destination.label)) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (screenFab.isVisible) {
                when (val fab = screenFab) {
                    is FabConfiguration.FabButton -> {
                        FloatingActionButton(
                            modifier = Modifier.imePadding(),
                            onClick = { fab.onClick() }
                        ) {
                            Icon(
                                imageVector = fab.icon,
                                contentDescription = stringResource(fab.iconLabelId)
                            )
                        }
                    }

                    is FabConfiguration.FabMenu -> {
                        FloatingActionButtonMenu(
                            modifier = Modifier.imePadding(),
                            expanded = fab.expanded,
                            button = {
                                ToggleFloatingActionButton(
                                    checked = fab.expanded,
                                    onCheckedChange = { fab.onExpandedChange(it) }
                                ) {
                                    Icon(
                                        imageVector = fab.icon,
                                        contentDescription = stringResource(fab.labelId)
                                    )
                                }
                            }
                        ) {
                            fab.items.forEach { item ->
                                FloatingActionButtonMenuItem(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    onClick = { item.onClick() },
                                    text = {
                                        item.extendedLabelId?.let {
                                            Text(text = stringResource(it))
                                        } ?: run {
                                            Text(text = stringResource(item.iconLabelId))
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = stringResource(item.iconLabelId)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    else -> {
                        FabConfiguration.Hidden()
                    }
                }
            } else {
                FabConfiguration.Hidden()
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { innerPadding ->
        MyVocabularyNavHost(
            navController = navController,
            startDestination = startDestination,
            onUpdateActions = { appBarActions = it },
            onUpdateTitle = { appBarTitle = it },
            onUpdateFab = { screenFab = it },
            isSearchVisible = isSearchVisible,
            onToggleSearch = { isSearchVisible = it },
            modifier = Modifier.padding(innerPadding),
            onBackClick = { newAction ->
                currentBackAction = newAction
            },
            isSortOpen = isSortOpen,
            onToggleSort = { isSortOpen = it },
        )
    }
}

