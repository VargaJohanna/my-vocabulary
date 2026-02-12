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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.navigation.MyVocabularyNavHost
import com.vocabulary.myvocabulary.navigation.MyVocabularyTopAppBar
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.ShareDictionaryViewModel
import com.vocabulary.myvocabulary.ui.theme.MyVocabularyTheme
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
        enableEdgeToEdge()
        setContent {
            MyVocabularyTheme {
                val navController = rememberNavController()
                MyVocabularyApp(navController)
            }

        }
        manageIntent(intent?.data)
        homeViewModel.openedAppCount()

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
//                                    findNavController(R.id.home_nav_host_fragment).navigate(R.id.dictionaryListFragment)
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
fun MyVocabularyApp(navController: NavHostController) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MyVocabularyTopAppBar(
                navController = navController,
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        MyVocabularyNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )

    }


}

