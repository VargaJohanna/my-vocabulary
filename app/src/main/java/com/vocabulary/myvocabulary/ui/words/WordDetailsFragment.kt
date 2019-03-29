package com.vocabulary.myvocabulary.ui.words

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import kotlinx.android.synthetic.main.dialog_rename_word.view.*
import kotlinx.android.synthetic.main.fragment_word_details.*
import kotlinx.android.synthetic.main.fragment_word_details.view.*
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
            setWordEditButtonClickListener(details_word, details_translation, word_edit)
            setWordDeleteButtonClickListener(word_delete)
        }
    }

    private fun setWordDeleteButtonClickListener(delete: ImageButton) {
        delete.setOnClickListener {
            showDeleteWordDialog()
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

    private fun setWordEditButtonClickListener(wordView: TextView, translationView: TextView, wordEdit: ImageView) {
        wordEdit.setOnClickListener {
            openWordEditDialog(wordView, translationView, wordCurrent)
        }
    }

    private fun openWordEditDialog(wordView: TextView, translationView: TextView, word: Word) {
        val inflater = requireActivity().layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_rename_word, null)
        val editTextWord: EditText = dialogView.rename_word_edit
        val editTextTranslation: EditText = dialogView.rename_translation_edit
        val saveButton: Button = dialogView.rename_and_close_button
        val cancelButton: TextView = dialogView.cancel_word_rename_button
        val errorMessageWord: TextView = dialogView.word_rename_error
        val errorMessageTranslation: TextView = dialogView.rename_translation_error

        val dialogBuilder = AlertDialog.Builder(requireActivity())
        wordEditDialog = dialogBuilder.create().apply {
            setView(dialogView)
            editTextWord.requestFocus()
            errorMessageWord.show(false)
            errorMessageTranslation.show(false)
            setupTextChangedListener(editTextWord, errorMessageWord)
            setupTextChangedListener(editTextTranslation, errorMessageTranslation)
            editTextWord.setText(word.word)
            editTextTranslation.setText(word.translation)
            saveButton.setOnClickListener {
                wordView.text = editTextWord.text.toString().trim()
                translationView.text = editTextTranslation.text.toString().trim()
                updateWord(word,
                        editTextWord,
                        editTextTranslation,
                        errorMessageWord,
                        errorMessageTranslation,
                        this)
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
            setTitle(R.string.edit_word_dialog_title)
            show()
        }
    }

    private fun updateWord(word: Word,
                           editTextWord: EditText,
                           editTextTranslation: EditText,
                           errorMessageWord: TextView,
                           errorMessageTranslation: TextView,
                           alertDialog: AlertDialog) {
        val inputWord = editTextWord.text.toString().trim()
        val inputTranslation = editTextTranslation.text.toString().trim()

        if (inputWord.isNotEmpty() && inputTranslation.isNotEmpty()) {
            errorMessageWord.show(false)
            errorMessageTranslation.show(false)
            wordCurrent = word.copy(word = inputWord, translation = inputTranslation)
            wordViewModel.updateWord(word.copy(word = inputWord, translation = inputTranslation))
            alertDialog.dismiss()
        } else if (inputWord.isEmpty() && inputTranslation.isEmpty()) {
            errorMessageWord.show(true)
            errorMessageTranslation.show(true)
        } else if (inputWord.isEmpty()) {
            errorMessageWord.show(true)
            errorMessageTranslation.show(false)
        } else if (inputTranslation.isEmpty()) {
            errorMessageWord.show(false)
            errorMessageTranslation.show(true)
        }
    }

    private fun setupTextChangedListener(editText: EditText, errorMessage: TextView) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                errorMessage.show(false)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                errorMessage.show(false)
            }

            override fun afterTextChanged(p0: Editable?) {
                errorMessage.show(false)
            }
        })
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

    private fun showDeleteWordDialog() {
        AlertDialog.Builder(requireActivity()).apply {
            setTitle(R.string.dialog_delete_word_title)
            setMessage("Are you sure you want to delete\n\"${wordCurrent.word} - ${wordCurrent.translation}\" ?")
            setPositiveButton(getString(R.string.delete_dialog_title)) { _, _ ->
                wordViewModel.deleteWord(wordCurrent)
                findNavController().popBackStack()
            }
            show()
        }
    }

    override fun onStop() {
        wordEditDialog?.dismiss()
        super.onStop()
    }
}