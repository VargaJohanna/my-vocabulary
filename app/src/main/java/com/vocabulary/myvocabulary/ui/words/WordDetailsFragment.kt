package com.vocabulary.myvocabulary.ui.words

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.vocabulary.myvocabulary.R
import kotlinx.android.synthetic.main.fragment_word_details.view.*

class WordDetailsFragment: Fragment() {
    private val args by navArgs<WordDetailsFragmentArgs>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_word_details, container, false).apply {
            details_word.text = args.word
            details_translation.text = args.translation
            been_asked_value.text = args.beenAsked.toString()
            passed_value.text = args.passed.toString()
            failed_value.text = args.failed.toString()
            last_result_value.text = if(args.lastResult) "Passed" else "Failed"
            word_details_toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        }
    }
}