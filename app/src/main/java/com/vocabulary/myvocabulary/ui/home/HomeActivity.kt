package com.vocabulary.myvocabulary.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.vocabulary.myvocabulary.R
import org.koin.androidx.viewmodel.ext.viewModel


class HomeActivity : AppCompatActivity() {
    private var importDialog: AlertDialog? = null
    private val homeViewModel: HomeViewModel by viewModel()

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        manageIntent(intent?.data)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        manageIntent(intent?.data)
    }

    private fun manageIntent(data: Uri?) {
        if (data != null) {
            homeViewModel.saveCsvData(data)
            homeViewModel.setIsImport(true)
            findNavController(R.id.home_nav_host_fragment).navigate(R.id.dictionaryListFragment)
        } else {
            homeViewModel.setIsImport(false)
        }
    }

    override fun onStop() {
        importDialog?.dismiss()
        super.onStop()
    }

}
