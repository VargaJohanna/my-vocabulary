package com.vocabulary.myvocabulary.ui.quizzes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vocabulary.myvocabulary.R
import kotlinx.android.synthetic.main.fragment_quiz_list.view.*

class QuizListFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_quiz_list, container, false).apply {
            full_quiz_card.setOnClickListener {
                findNavController().navigate(R.id.to_dictionaryPickerFragment)
            }
        }
    }
}