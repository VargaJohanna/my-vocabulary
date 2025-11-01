package com.vocabulary.myvocabulary.ui.quizzes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.repositories.quiz.CustomQuizRepository
import com.vocabulary.myvocabulary.utils.DialogFactory
import org.koin.android.ext.android.inject
import com.vocabulary.myvocabulary.databinding.FragmentQuizListBinding

class QuizListFragment : Fragment() {
    private var _binding: FragmentQuizListBinding? = null
    private val binding get() = _binding!!
    private val dialogFactory: DialogFactory by inject()
    private val customQuizRepository: CustomQuizRepository by inject()
    private var customQuizDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuizListBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.fullQuizCard.setOnClickListener {
            val action =
                QuizListFragmentDirections.toDictionaryPickerFragment(QuizTypes.FullQuiz.toInt())
            findNavController().navigate(action)
        }

        binding.fullQuizInfoButton.setOnClickListener {
            dialogFactory.buildInfoDialog(
                requireActivity(),
                getString(R.string.quiz_list_ask_me_everything),
                getString(R.string.quick_list_info)
            ).show()
        }

        binding.quickQuizCard.setOnClickListener {
            val action =
                QuizListFragmentDirections.toDictionaryPickerFragment(QuizTypes.QuickQuiz.toInt())
            findNavController().navigate(action)
        }

        binding.quickInfoButton.setOnClickListener {
            dialogFactory.buildInfoDialog(
                requireActivity(),
                getString(R.string.quiz_list_quick_one),
                getString(R.string.ask_everything_info)
            ).show()
        }

        binding.weaknessQuizCard.setOnClickListener {
            val action =
                QuizListFragmentDirections.toDictionaryPickerFragment(QuizTypes.WeakestQuiz.toInt())
            findNavController().navigate(action)
        }

        binding.weaknessesInfoButton.setOnClickListener {
            dialogFactory.buildInfoDialog(
                requireActivity(),
                getString(R.string.quiz_list_weaknesses),
                getString(R.string.weaknesses_info)
            ).show()
        }

        binding.customQuizCard.setOnClickListener {
            showCustomQuizDialog()
        }

        binding.customInfoButton.setOnClickListener {
            dialogFactory.buildInfoDialog(
                requireActivity(),
                getString(R.string.quiz_list_custom),
                getString(R.string.custom_info)
            ).show()
        }

        binding.quizListToolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        return view
    }

    private fun showCustomQuizDialog() {
        customQuizDialog = dialogFactory.buildCustomQuizSizeDialog(
            activity = requireActivity(),
            titleText = getString(R.string.custom_dialog_title)
        ) { size ->
            customQuizRepository.quizSize = size

            val action =
                QuizListFragmentDirections.toDictionaryPickerFragment(QuizTypes.CustomQuiz.toInt())
            findNavController().navigate(action)
        }
        customQuizDialog?.show()
    }

    override fun onDestroy() {
        customQuizDialog?.dismiss()
        super.onDestroy()
    }
}