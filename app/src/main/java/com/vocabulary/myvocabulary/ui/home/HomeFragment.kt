package com.vocabulary.myvocabulary.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.utils.DialogFactory
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.viewModel

class HomeFragment : Fragment() {
    private val dialogFactory: DialogFactory by inject()
    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false).apply {
            dictionary_button.setOnClickListener { it.findNavController().navigate(R.id.action_homeFragment_to_dictionaryListFragment) }
            quiz_button.setOnClickListener { it.findNavController().navigate(R.id.action_homeFragment_to_quizListFragment) }
            about_button.setOnClickListener { it.findNavController().navigate(R.id.to_aboutFragment) }
            quote_button.setOnClickListener {
                showQuote(home_progress_bar)
            }

        }
    }

    private fun showQuote(progressbar: ProgressBar) {
        progressbar.show(true)
        homeViewModel.liveQuote.observe(requireActivity(), Observer {
            progressbar.show(false)
            dialogFactory.buildInfoDialog(
                    requireActivity(),
                    it.title,
                    it.quote
            ).show()
        })
    }
}