package com.vocabulary.myvocabulary.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.vocabulary.myvocabulary.R
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false).apply {
            dictionary_button.setOnClickListener { it.findNavController().navigate(R.id.action_homeFragment_to_dictionaryListFragment) }
            quiz_button.setOnClickListener { it.findNavController().navigate(R.id.action_homeFragment_to_quizListFragment) }
        }
    }
}