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
                        getString(R.string.quiz_list_ask_me_everything),
                        getString(R.string.quick_list_info)
                ).show()
            }

            quick_quiz_card.setOnClickListener {
                val action = QuizListFragmentDirections.toDictionaryPickerFragment(QuizTypes.QuickQuiz.toInt())
                findNavController().navigate(action)
            }

            quick_info_button.setOnClickListener {
                dialogFactory.buildInfoDialog(
                        requireActivity(),
                        getString(R.string.quiz_list_quick_one),
                        getString(R.string.ask_everything_info)
                ).show()
            }

            weakness_quiz_card.setOnClickListener {
                val action = QuizListFragmentDirections.toDictionaryPickerFragment(QuizTypes.WeakestQuiz.toInt())
                findNavController().navigate(action)
            }

            weaknesses_info_button.setOnClickListener {
                dialogFactory.buildInfoDialog(
                        requireActivity(),
                        getString(R.string.quiz_list_weaknesses),
                        getString(R.string.weaknesses_info)
                ).show()
            }

            quiz_list_toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        }
    }
}