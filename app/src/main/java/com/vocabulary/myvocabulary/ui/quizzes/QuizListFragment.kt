package com.vocabulary.myvocabulary.ui.quizzes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.utils.DialogFactory
import kotlinx.android.synthetic.main.fragment_quiz_list.view.*
import org.koin.android.ext.android.inject

class QuizListFragment : Fragment() {
    private val dialogFactory: DialogFactory by inject()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_quiz_list, container, false).apply {
            full_quiz_card.setOnClickListener {
                val action = QuizListFragmentDirections.toDictionaryPickerFragment(QuizTypes.FullQuiz.toInt())
                findNavController().navigate(action)
            }

            full_quiz_info_button.setOnClickListener {
                dialogFactory.buildInfoDialog(
                        requireActivity(),
                        "Ask Me Everything",
                        "Asks all the expressions from the selected dictionary"
                ).show()
            }

            quick_quiz_card.setOnClickListener {
                val action = QuizListFragmentDirections.toDictionaryPickerFragment(QuizTypes.QuickQuiz.toInt())
                findNavController().navigate(action)
            }

            quick_info_button.setOnClickListener {
                dialogFactory.buildInfoDialog(
                        requireActivity(),
                        "Quick One",
                        "Asks 5 random expressions from the selected dictionary"
                ).show()
            }

            weakness_quiz_card.setOnClickListener {
                val action = QuizListFragmentDirections.toDictionaryPickerFragment(QuizTypes.WeakestQuiz.toInt())
                findNavController().navigate(action)
            }

            weaknesses_info_button.setOnClickListener {
                dialogFactory.buildInfoDialog(
                        requireActivity(),
                        "Weaknesses",
                        "Asks the 5 most failed expressions from the selected dictionary"
                ).show()
            }

            quiz_list_toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        }
    }
}