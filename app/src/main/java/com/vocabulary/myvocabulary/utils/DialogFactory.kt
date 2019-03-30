package com.vocabulary.myvocabulary.utils

import android.app.Activity
import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.ui.words.Word
import kotlinx.android.synthetic.main.dialog_rename_word.view.*

class DialogFactory {

    fun openEditDialog(
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
}