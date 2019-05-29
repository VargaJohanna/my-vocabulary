package com.vocabulary.myvocabulary.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.ShareDictionaryViewModel
import com.vocabulary.myvocabulary.utils.DialogFactory
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.viewModel
import java.util.*

class HomeActivity : AppCompatActivity() {
    private var importDialog: AlertDialog? = null
    private val homeViewModel: HomeViewModel by viewModel()
    private val shareViewModel: ShareDictionaryViewModel by viewModel()
    private val dialogFactory: DialogFactory by inject()

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        manageIntent(intent?.data)

        importDictionary()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        manageIntent(intent?.data)
    }

    private fun manageIntent(data: Uri?) {
        if (data != null) {
            homeViewModel.saveCsvData(data)
            homeViewModel.setIsImport(true)
        } else {
            homeViewModel.setIsImport(false)
        }
    }

    override fun onStop() {
        importDialog?.dismiss()
        super.onStop()
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
                        shareViewModel.createDictionary(Dictionary(
                                dictionaryName = nameToCreate,
                                dictionaryCreated = Calendar.getInstance().time))

                        // TODO: Fix me
                        shareViewModel.getImportedDictionaryDetails().observe(this, Observer { event ->
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
