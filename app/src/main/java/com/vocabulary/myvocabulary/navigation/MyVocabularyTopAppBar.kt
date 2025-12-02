package com.vocabulary.myvocabulary.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.vocabulary.myvocabulary.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MyVocabularyTopAppBar(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {

    TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) },
        navigationIcon = {
            if (navController.previousBackStackEntry != null) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_arrow)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.home_info)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
//    val backStackEntry by navController.currentBackStackEntryAsState()
//
//    // Fallback to the local owner if the backStackEntry is null during initial composition.
//    val viewModelStoreOwner = backStackEntry ?: LocalViewModelStoreOwner.current
//
//    // Only proceed to get the ViewModel and show the TopAppBar if the owner is not null.
//    viewModelStoreOwner?.let { owner ->
//        val viewModel: TopAppBarViewModel =
//            viewModel(viewModelStoreOwner = owner, initializer = { TopAppBarViewModel() })
//
//        TopAppBar(
//            scrollBehavior = scrollBehavior,
//            modifier = modifier,
//            title = { viewModel.title },
//            navigationIcon = {
//                if (navController.previousBackStackEntry != null) {
//                    IconButton(onClick = { navController.navigateUp() }) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = stringResource(R.string.back_arrow)
//                        )
//                    }
//                }
//            },
//            actions = {
//                IconButton(onClick = {
//                    navController.navigate(About)
//                }) {
//                    Icon(
//                        imageVector = Icons.Default.Info,
//                        contentDescription = stringResource(R.string.home_info)
//                    )
//                }
//            },
//        )
//    }
}

/**
 * A helper composable to set the title for the current screen in the TopAppBar's ViewModel.
 * Call this function from within any screen that needs to display a title.
 *
 * @param title A composable lambda that renders the title (e.g., using Text).
 */
@Composable
fun ProvideAppBarTitle(title: @Composable () -> Unit) {
    // Safely get the ViewModelStoreOwner for the current screen.
    // Inside a NavHost destination, LocalViewModelStoreOwner.current is guaranteed to be non-null.
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
        ?: error("ProvideAppBarTitle should be called within a NavHost destination.")

    // Get the ViewModel scoped to the current screen.

    val viewModel: TopAppBarViewModel = viewModel(viewModelStoreOwner = viewModelStoreOwner)


    // Use LaunchedEffect to update the title in the ViewModel.
    // This ensures the update happens safely within the composition lifecycle.
    LaunchedEffect(title) {
        viewModel.title = title
    }

}

// ViewModel to hold the state for the TopAppBar, scoped to a navigation destination.
class TopAppBarViewModel : ViewModel() {
    var title by mutableStateOf<@Composable () -> Unit>({ }, referentialEqualityPolicy())
}