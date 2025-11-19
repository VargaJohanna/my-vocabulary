package com.vocabulary.myvocabulary.ui.home

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.findNavController
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.navigation.MyVocabularyNavHost
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.ShareDictionaryViewModel
import com.vocabulary.myvocabulary.ui.theme.ComposeTheme
import com.vocabulary.myvocabulary.utils.DialogFactory
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class HomeActivity : ComponentActivity() {
    private var importDialog: AlertDialog? = null
    private val homeViewModel: HomeViewModel by viewModel()
    private val shareViewModel: ShareDictionaryViewModel by viewModel()
    private val dialogFactory: DialogFactory by inject()
    val padding = 16.dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            enableEdgeToEdge()
            MyVocabularyApp()

        }
//        setContentView(R.layout.activity_home)
        manageIntent(intent?.data)
        homeViewModel.openedAppCount()
        importDictionary()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = findNavController(R.id.home_nav_host_fragment)
                val isOnWordListScreen =
                    navController.currentDestination?.id == R.id.wordListFragment

                if (isOnWordListScreen && homeViewModel.searchBarState()) {
                    homeViewModel.setSearchBarState(false)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

//TODO: What is this for?
//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        manageIntent(intent?.data)
//    }

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

    private fun importDictionary() {
        shareViewModel.getLiveIsImport().observe(this, Observer { isImport ->
            if (isImport) {
                shareViewModel.setIsImport(false)
                if (importDialog == null || importDialog!!.isShowing.not()) {
                    importDialog = dialogFactory.buildDictionaryCreateDialog(
                        this,
                        getString(R.string.import_dictionary_dialog_title)
                    ) { nameToCreate ->
                        shareViewModel.createDictionary(
                            Dictionary(
                                dictionaryName = nameToCreate,
                                dictionaryCreated = Calendar.getInstance().time
                            )
                        )

                        // TODO: Fix me
                        shareViewModel.getImportedDictionaryDetails()
                            .observe(this, Observer { event ->
                                event.getContentIfNotHandled()?.let {
                                    shareViewModel.parseDataAndCreateWords(it.dictionaryId, this)

                                    importDialog?.dismiss()
                                    findNavController(R.id.home_nav_host_fragment).navigate(R.id.dictionaryListFragment)
                                }
                            })
                    }
                    importDialog?.show()
                }
            }
        })
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyVocabularyApp() {
    ComposeTheme {
        val navController = rememberNavController()
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentDestination = currentBackStack?.destination
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {Text(text = "My Vocabulary")},
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.navigateUp()
                        }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Default.Info,
                                contentDescription = "Info about developer")
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
            ) { innerPadding ->
            MyVocabularyNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding))

        }

    }
}

