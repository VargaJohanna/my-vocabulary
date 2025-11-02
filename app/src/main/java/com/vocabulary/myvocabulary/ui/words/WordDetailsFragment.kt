package com.vocabulary.myvocabulary.ui.words

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.utils.DialogFactory
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import com.vocabulary.myvocabulary.databinding.FragmentWordDetailsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel


class WordDetailsFragment : Fragment() {
    private var _binding: FragmentWordDetailsBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<WordDetailsFragmentArgs>()
    private val wordViewModel: WordListViewModel by viewModel() {
        parametersOf(args.dictionaryId)
    }
    private val wordDetailViewModel: WordDetailsViewModel by viewModel()
    private val dialogFactory: DialogFactory by inject()
    private var wordEditDialog: AlertDialog? = null
    private lateinit var wordCurrent: Word

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentWordDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        wordDetailViewModel.getWordById(args.wordId)
        wordDetailViewModel.getCurrentWord().observe(requireActivity(), androidx.lifecycle.Observer {
            wordCurrent = it
            setView(it)
        })
        binding.wordDetailsToolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        setWordEditButtonClickListener(binding.detailsWord, binding.detailsTranslation, binding.wordEditButton)
        setWordDeleteButtonClickListener(binding.wordDeleteButton)
        return view
    }

    private fun setView(word: Word) {
        binding.detailsWord.text = word.word
        binding.detailsTranslation.text = word.translation
        binding.beenAskedValue.text = word.beenAsked.toString()
        binding.passedValue.text = word.passed.toString()
        binding.failedValue.text = word.failed.toString()
        binding.lastResultValue.text = if (word.lastResult) requireActivity().getString(R.string.passed) else requireActivity().getString(R.string.failed)
        binding.createdValue.text = formatDate(word.created)
    }

    private fun setWordDeleteButtonClickListener(delete: ImageButton) {
        delete.setOnClickListener {
            dialogFactory.buildDeleteWordDialog(
                    requireActivity(),
                    getString(R.string.dialog_delete_word_title),
                    "${getString(R.string.verify_deletion)}\n\"${wordCurrent.translation} - ${wordCurrent.word}\" ?"
            ) {
                wordViewModel.deleteWord(wordCurrent)
                findNavController().popBackStack()
            }.show()
        }
    }

    private fun setWordEditButtonClickListener(wordView: TextView, translationView: TextView, wordEdit: ImageView) {
        wordEdit.setOnClickListener {
            wordEditDialog = dialogFactory.buildWordEditDialog(
                    requireActivity(),
                    wordCurrent
            ) { word, otherWord ->
                wordView.text = word
                translationView.text = otherWord
                wordCurrent = wordCurrent.copy(word = word, translation = otherWord)
                wordViewModel.updateWord(wordCurrent.copy(word = word, translation = otherWord))
                wordEditDialog?.dismiss()

            }
            wordEditDialog?.show()
        }
    }

    private fun formatDate(date: Date): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
            date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(formatter).toString()
        } else {
            val dateFormat = SimpleDateFormat.getDateInstance()
            dateFormat.format(date).toString()
        }
    }

    override fun onDestroy() {
        wordEditDialog?.dismiss()
        super.onDestroy()
    }
}