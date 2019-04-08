package com.vocabulary.myvocabulary.ui.words

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.utils.DialogFactory
import kotlinx.android.synthetic.main.fragment_word_details.*
import kotlinx.android.synthetic.main.fragment_word_details.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.viewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class WordDetailsFragment : Fragment() {
    private val args by navArgs<WordDetailsFragmentArgs>()
    private val wordViewModel: WordListViewModel by viewModel {
        parametersOf(args.dictionaryId)
    }
    private val wordDetailViewModel: WordDetailsViewModel by viewModel()
    private val dialogFactory: DialogFactory by inject()
    private var wordEditDialog: AlertDialog? = null
    private lateinit var wordCurrent: Word

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        wordDetailViewModel.getWordById(args.wordId)
        wordDetailViewModel.getCurrentWord().observe(requireActivity(), androidx.lifecycle.Observer {
            wordCurrent = it
            setView(it)
        })
        return inflater.inflate(R.layout.fragment_word_details, container, false).apply {
            word_details_toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
            setWordEditButtonClickListener(details_word, details_translation, word_edit_button)
            setWordDeleteButtonClickListener(word_delete_button)
        }
    }

    private fun setView(word: Word) {
        details_word.text = word.word
        details_translation.text = word.translation
        been_asked_value.text = word.beenAsked.toString()
        passed_value.text = word.passed.toString()
        failed_value.text = word.failed.toString()
        last_result_value.text = if (word.lastResult) requireActivity().getString(R.string.passed) else requireActivity().getString(R.string.failed)
        created_value.text = formatDate(word.created)
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

    override fun onStop() {
        wordEditDialog?.dismiss()
        super.onStop()
    }
}