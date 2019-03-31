package com.vocabulary.myvocabulary.utils

import android.app.Activity
import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.words.Word
import kotlinx.android.synthetic.main.dialog_create_dictionary.view.*
import kotlinx.android.synthetic.main.dialog_create_word.view.*
import kotlinx.android.synthetic.main.dialog_rename_dictionary.view.*
import kotlinx.android.synthetic.main.dialog_rename_word.view.*
import kotlinx.android.synthetic.main.dialog_start_quiz.view.*

class DialogFactory {

    fun buildWordEditDialog(
            activity: Activity,
            word: Word,
            saveClick: (word: String, translation: String) -> Unit
    ): AlertDialog {
        val dialogView: View = activity.layoutInflater.inflate(R.layout.dialog_rename_word, null)
        val editTextWord: EditText = dialogView.rename_word_edit
        val editTextTranslation: EditText = dialogView.rename_translation_edit
        val saveButton: Button = dialogView.rename_and_close_button
        val cancelButton: TextView = dialogView.cancel_word_rename_button
        val errorMessageWord: TextView = dialogView.word_rename_error
        val errorMessageTranslation: TextView = dialogView.rename_translation_error

        val dialogBuilder = AlertDialog.Builder(activity)
        return dialogBuilder.create().apply {
            setView(dialogView)
            editTextWord.requestFocus()
            errorMessageWord.show(false)
            errorMessageTranslation.show(false)
            setupTextChangedListener(editTextWord, errorMessageWord)
            setupTextChangedListener(editTextTranslation, errorMessageTranslation)
            editTextWord.setText(word.word)
            editTextTranslation.setText(word.translation)
            saveButton.setOnClickListener {
                val inputWord = editTextWord.text.toString().trim()
                val inputTranslation = editTextTranslation.text.toString().trim()

                if (inputWord.isNotEmpty() && inputTranslation.isNotEmpty()) {
                    errorMessageWord.show(false)
                    errorMessageTranslation.show(false)
                    saveClick(
                            editTextWord.text.toString().trim(),
                            editTextTranslation.text.toString().trim()
                    )
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
            cancelButton.setOnClickListener {
                dismiss()
            }
            setTitle(R.string.edit_word_dialog_title)
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

    fun buildDeleteWordDialog(
            activity: Activity,
            title: String,
            message: String,
            deleteClick: () -> Unit
    ): AlertDialog.Builder {
        return AlertDialog.Builder(activity).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(R.string.delete_dialog_title) { _, _ ->
                deleteClick()
            }
        }
    }

    fun buildStartQuizDialog(
            dictionaryId: Long,
            activity: Activity,
            dictionaryName: String,
            doItClick: (selectedDirection: Int, dictionaryId: Long, selectedQuizType: Int) -> Unit
    ): AlertDialog {
        var selectedDirection = -1
        var selectedQuizType = -1
        val inflater = activity.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_start_quiz, null)
        val directionRadioGroup: RadioGroup = dialogView.direction_radioGroup
        val quizTypeRadioGroup: RadioGroup = dialogView.quiz_type_radioGroup
        val doItButton: Button = dialogView.from_dictionary_lets_do_it
        val cancelButton: Button = dialogView.from_dictionary_cancel
        val directionErrorMessage: TextView = dialogView.from_dictionary_option_picker_error
        val quizTypeErrorMessage: TextView = dialogView.from_dictionary_quiz_type_error
        val dialogBuilder = AlertDialog.Builder(activity)
        return dialogBuilder.create().apply {
            setView(dialogView)
            directionRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                directionErrorMessage.show(false)
                selectedDirection = if (checkedId == R.id.from_dictionary_word_radio) 0 else 1
            }
            quizTypeRadioGroup.setOnCheckedChangeListener { _, checkedTypeId ->
                quizTypeErrorMessage.show(false)
                selectedQuizType = when (checkedTypeId) {
                    R.id.quick_quiz_radio -> 0
                    R.id.full_quiz_radio -> 1
                    R.id.weakness_quiz_radio -> 2
                    else -> throw IllegalStateException("Unknown quiz type: $this")
                }
            }

            doItButton.setOnClickListener {
                if (selectedDirection == -1 || selectedQuizType == -1) {
                    if (selectedDirection == -1) directionErrorMessage.show(true)
                    if (selectedQuizType == -1) quizTypeErrorMessage.show(true)
                } else {
                    doItClick(selectedDirection, dictionaryId, selectedQuizType)
                }
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
            setTitle("${activity.getString(R.string.dictionary_menu_start_quiz)} of \"$dictionaryName\"")
        }
    }

    fun buildWordCreateDialog(
            activity: Activity,
            createClick: (wordText: String, translationText: String) -> Unit,
            addMoreClick: (wordText: String, translationText: String) -> Unit

    ): AlertDialog {
        val inflater = activity.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_create_word, null)
        val editTextWord: EditText = dialogView.new_word_edit
        val editTextTranslation: EditText = dialogView.new_translation_edit
        val saveButton: Button = dialogView.create_and_close_button
        val addMoreButton: Button = dialogView.create_and_keep_adding_button
        val cancelButton: TextView = dialogView.cancel_word_adding_button
        val errorMessageWord: TextView = dialogView.word_name_error
        val errorMessageTranslation: TextView = dialogView.word_translation_error

        val dialogBuilder = AlertDialog.Builder(activity)
        return dialogBuilder.create().apply {
            setView(dialogView)
            editTextWord.requestFocus()
            errorMessageWord.show(false)
            errorMessageTranslation.show(false)
            setupTextChangedListener(editTextWord, errorMessageWord)
            setupTextChangedListener(editTextTranslation, errorMessageTranslation)
            saveButton.setOnClickListener {
                val inputWord = editTextWord.text.toString().trim()
                val inputTranslation = editTextTranslation.text.toString().trim()
                if (inputWord.isNotEmpty() && inputTranslation.isNotEmpty()) {
                    errorMessageWord.show(false)
                    errorMessageTranslation.show(false)
                    createClick(
                            editTextWord.text.toString().trim(),
                            editTextTranslation.text.toString().trim())
                    editTextWord.setText("")
                    editTextTranslation.setText("")
                    editTextWord.requestFocus()

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
            addMoreButton.setOnClickListener {
                val inputWord = editTextWord.text.toString().trim()
                val inputTranslation = editTextTranslation.text.toString().trim()
                if (inputWord.isNotEmpty() && inputTranslation.isNotEmpty()) {
                    errorMessageWord.show(false)
                    errorMessageTranslation.show(false)
                    addMoreClick(
                            editTextWord.text.toString().trim(),
                            editTextTranslation.text.toString().trim()
                    )
                    editTextWord.setText("")
                    editTextTranslation.setText("")
                    editTextWord.requestFocus()

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

            cancelButton.setOnClickListener {
                dismiss()
            }
            setTitle(R.string.create_new_word_dialog_title)
        }
    }

    fun buildDictionaryRenameDialog(
            activity: Activity,
            dictionary: Dictionary,
            renameClick: (renameTo: String) -> Unit): AlertDialog {
        val inflater = activity.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_rename_dictionary, null)
        val editText: EditText = dialogView.rename_dictionary_edit
        val renameButton: Button = dialogView.rename_dictionary_button
        val cancelButton: Button = dialogView.cancel_dictionary_rename_dialog
        val errorMessage: TextView = dialogView.dictionary_name_error_rename
        val dialogBuilder = AlertDialog.Builder(activity)
        return dialogBuilder.create().apply {
            setView(dialogView)
            errorMessage.show(false)
            setupTextChangedListener(editText, errorMessage)
            cancelButton.setOnClickListener {
                errorMessage.show(false)
                dismiss()
            }
            setTitle("Renaming \"${dictionary.dictionaryName}\" dictionary")
            renameButton.setOnClickListener {
                if (editText.text.toString().isNotEmpty()) {
                    errorMessage.show(false)
                    renameClick(editText.text.toString())

                } else {
                    errorMessage.show(true)
                }
            }
        }
    }

    fun buildDictionaryCreateDialog(
            activity: Activity,
            createClick: (nameToCreate: String) -> Unit
    ): AlertDialog {
        val inflater = activity.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_create_dictionary, null)
        val editText: EditText = dialogView.new_dictionary_edit
        val createButton: Button = dialogView.create_dictionary_button
        val cancelButton: Button = dialogView.cancel_dictionary_creation
        val errorMessage: TextView = dialogView.dictionary_name_error

        val dialogBuilder = AlertDialog.Builder(activity)
        return dialogBuilder.create().apply {
            setView(dialogView)
            errorMessage.show(false)
            setupTextChangedListener(editText, errorMessage)
            createButton.setOnClickListener {
                if (editText.text.toString().isNotEmpty()) {
                    errorMessage.show(false)
                    createClick(editText.text.toString())

                } else {
                    errorMessage.show(true)
                }
            }
            cancelButton.setOnClickListener {
                errorMessage.show(false)
                dismiss()
            }
            setTitle(activity.getString(R.string.create_new_dictionary_dialog_title))
        }
    }

    fun buildInfoDialog(
            activity: Activity,
            title: String,
            message: String
    ): AlertDialog.Builder {
        return AlertDialog.Builder(activity).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(activity.getString(R.string.info_dialog)) { dialog, _ ->
                dialog.dismiss()
            }
        }
    }
}