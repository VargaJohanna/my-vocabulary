package com.vocabulary.myvocabulary.ui.home

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.vocabulary.myvocabulary.R


class HomeActivity : AppCompatActivity() {

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        if(intent.data != null) {
            findNavController(R.id.home_nav_host_fragment).navigate(R.id.dictionaryListFragment)
        }
    }
}
