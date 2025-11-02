package com.vocabulary.myvocabulary.utils

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.ext.stringToInt
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.databinding.DialogAddQuizSizeBinding
import com.vocabulary.myvocabulary.databinding.DialogCreateWordBinding
import com.vocabulary.myvocabulary.databinding.DialogCreateDictionaryBinding
import com.vocabulary.myvocabulary.databinding.DialogDirectionOptionPickerBinding
import com.vocabulary.myvocabulary.databinding.DialogQuoteBinding
import com.vocabulary.myvocabulary.databinding.DialogRenameDictionaryBinding
import com.vocabulary.myvocabulary.databinding.DialogRenameWordBinding
import com.vocabulary.myvocabulary.databinding.DialogStartQuizBinding

class DialogFactory {

    fun buildWordEditDialog(
        activity: Activity,
        word: Word,
        saveClick: (word: String, translation: String) -> Unit
    ): AlertDialog {
        val binding = DialogRenameWordBinding.inflate(activity.layoutInflater)
        val editTextWord: EditText = binding.renameWordEdit
        val editTextTranslation: EditText = binding.renameTranslationEdit
        val saveButton: Button = binding.renameAndCloseButton
        val cancelButton: TextView = binding.cancelWordRenameButton
        val errorMessageWord: TextView = binding.wordRenameError
        val errorMessageTranslation: TextView = binding.renameTranslationError

        val dialogBuilder = AlertDialog.Builder(activity)
        return dialogBuilder.create().apply {
            setView(binding.root)
            editTextTranslation.requestFocus()
            errorMessageWord.show(false)
            errorMessageTranslation.show(false)
            setupTextChangedListener(editTextWord, errorMessageWord)
            setupTextChangedListener(editTextTranslation, errorMessageTranslation)
            editTextWord.setText(word.word)
            editTextTranslation.setText(word.translation)
            saveButton.setOnClickListener {
                val inputWord = editTextWord.text.toString().trim()
                val inputTranslation = editTextTranslation.text.toString().trim()

                if (inputTranslation.isNotEmpty()) {
                    errorMessageTranslation.show(false)
                    saveClick(
                        inputWord,
                        inputTranslation
                    )
                } else errorMessageTranslation.show(true)
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
        doItClick: (selectedDirection: Int, dictionaryId: Long, selectedQuizType: Int, quizSize: Int?) -> Unit
    ): AlertDialog {
        var selectedDirection = 0
        var selectedQuizType = 0
        val binding = DialogStartQuizBinding.inflate(activity.layoutInflater)
        val directionRadioGroup: RadioGroup = binding.directionRadioGroup
        val quizTypeRadioGroup: RadioGroup = binding.quizTypeRadioGroup
        val doItButton: Button = binding.fromDictionaryLetsDoIt
        val cancelButton: Button = binding.fromDictionaryCancel
        val customEditText: TextInputEditText = binding.customQuizSize
        val textLayout: TextInputLayout = binding.customQuizLayout
        val dialogBuilder = AlertDialog.Builder(activity)
        return dialogBuilder.create().apply {
            setView(binding.root)
            directionRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                selectedDirection = if (checkedId == R.id.from_dictionary_word_radio) 0 else 1
            }
            quizTypeRadioGroup.setOnCheckedChangeListener { _, checkedTypeId ->
                when (checkedTypeId) {
                    R.id.quick_quiz_radio -> {
                        selectedQuizType = 0
                        textLayout.visibility = View.GONE
                    }

                    R.id.full_quiz_radio -> {
                        selectedQuizType = 1
                        textLayout.visibility = View.GONE
                    }

                    R.id.weakness_quiz_radio -> {
                        selectedQuizType = 2
                        textLayout.visibility = View.GONE
                    }

                    R.id.custom_quiz_radio -> {
                        selectedQuizType = 3
                        textLayout.visibility = View.VISIBLE
                    }

                    else -> throw IllegalStateException("Unknown quiz type: $this")
                }
                selectedQuizType = when (checkedTypeId) {
                    R.id.quick_quiz_radio -> 0
                    R.id.full_quiz_radio -> 1
                    R.id.weakness_quiz_radio -> 2
                    R.id.custom_quiz_radio -> 3
                    else -> throw IllegalStateException("Unknown quiz type: $this")
                }
            }

            doItButton.setOnClickListener {
                if (selectedQuizType == 3) {
                    customEditText.text?.let {
                        if (it.isEmpty() || it.toString() == "0") {
                            textLayout.error = activity.getString(R.string.custom_dialog_error)
                        } else {
                            doItClick(
                                selectedDirection,
                                dictionaryId,
                                selectedQuizType,
                                it.toString().stringToInt()
                            )
                            dismiss()
                        }
                    }
                } else {
                    doItClick(
                        selectedDirection,
                        dictionaryId,
                        selectedQuizType,
                        customEditText.text.toString().stringToInt()
                    )
                    dismiss()
                }

            }
            cancelButton.setOnClickListener {
                dismiss()
            }
            setTitle("${activity.getString(R.string.dictionary_menu_start_quiz)} \"$dictionaryName\"")
            customEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    textLayout.error = null
                }
            })
        }
    }

    fun buildWordCreateDialog(
        activity: Activity,
        createClick: (wordText: String, translationText: String) -> Unit,
        addMoreClick: (wordText: String, translationText: String) -> Unit

    ): AlertDialog {
        val binding = DialogCreateWordBinding.inflate(activity.layoutInflater)
        val editTextWord: EditText = binding.newWordEdit
        val editTextTranslation: EditText = binding.newTranslationEdit
        val saveButton: Button = binding.createAndCloseButton
        val addMoreButton: Button = binding.createAndKeepAddingButton
        val cancelButton: TextView = binding.cancelWordAddingButton
        val errorMessageWord: TextView = binding.wordNameError
        val errorMessageTranslation: TextView = binding.wordTranslationError

        val dialogBuilder = AlertDialog.Builder(activity)
        return dialogBuilder.create().apply {
            setView(binding.root)
            editTextTranslation.requestFocus()
            errorMessageWord.show(false)
            errorMessageTranslation.show(false)
            setupTextChangedListener(editTextWord, errorMessageWord)
            setupTextChangedListener(editTextTranslation, errorMessageTranslation)
            saveButton.setOnClickListener {
                val inputWord = editTextWord.text.toString().trim()
                val inputTranslation = editTextTranslation.text.toString().trim()
                if (inputTranslation.isNotEmpty()) {
                    errorMessageTranslation.show(false)
                    createClick(
                        inputWord,
                        inputTranslation
                    )
                    editTextWord.setText("")
                    editTextTranslation.setText("")
                    editTextTranslation.requestFocus()

                } else {
                    errorMessageWord.show(false)
                    errorMessageTranslation.show(true)
                }
            }
            addMoreButton.setOnClickListener {
                val inputWord = editTextWord.text.toString().trim()
                val inputTranslation = editTextTranslation.text.toString().trim()
                if (inputTranslation.isNotEmpty()) {
                    errorMessageTranslation.show(false)
                    addMoreClick(
                        inputWord,
                        inputTranslation
                    )
                    editTextWord.setText("")
                    editTextTranslation.setText("")
                    editTextTranslation.requestFocus()

                } else {
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
        renameClick: (renameTo: String) -> Unit
    ): AlertDialog {
        val binding = DialogRenameDictionaryBinding.inflate(activity.layoutInflater)
        val editText: EditText = binding.renameDictionaryEdit
        val renameButton: Button = binding.renameDictionaryButton
        val cancelButton: Button = binding.cancelDictionaryRenameDialog
        val errorMessage: TextView = binding.dictionaryNameErrorRename
        val dialogBuilder = AlertDialog.Builder(activity)
        return dialogBuilder.create().apply {
            setView(binding.root)
            errorMessage.show(false)
            setupTextChangedListener(editText, errorMessage)
            cancelButton.setOnClickListener {
                errorMessage.show(false)
                dismiss()
            }
            setTitle("${activity.getString(R.string.renaming_dictionary_title)} \"${dictionary.dictionaryName}\"")
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
        title: String,
        createClick: (nameToCreate: String) -> Unit
    ): AlertDialog {
        val binding = DialogCreateDictionaryBinding.inflate(activity.layoutInflater)
        val editText: EditText = binding.newDictionaryEdit
        val createButton: Button = binding.createDictionaryButton
        val cancelButton: Button = binding.cancelDictionaryCreation
        val errorMessage: TextView = binding.dictionaryNameError

        val dialogBuilder = AlertDialog.Builder(activity)
        return dialogBuilder.create().apply {
            setView(binding.root)
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
            setTitle(title)
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

    fun buildOptionsDialog(
        activity: Activity,
        doItClick: (selectedOption: Int) -> Unit
    ): AlertDialog {
        var selectedOption = 0
        val binding = DialogDirectionOptionPickerBinding.inflate(activity.layoutInflater)
        val radioGroup: RadioGroup = binding.radioGroup
        val doItButton: Button = binding.letsDoItButton
        val cancelButton: Button = binding.cancelDirectionPickerDialog
        val errorMessage: TextView = binding.optionPickerError
        val dialogBuilder = AlertDialog.Builder(activity)
        return dialogBuilder.create().apply {
            setView(binding.root)
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                errorMessage.show(false)
                selectedOption = if (checkedId == R.id.word_radio) 0 else 1
            }
            doItButton.setOnClickListener {
                if (selectedOption == -1) {
                    errorMessage.show(true)
                } else {
                    doItClick(selectedOption)
                    dismiss()
                }
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
            setTitle(R.string.dialog_pick_direction)
        }
    }

    fun buildQuotesDialog(
        activity: Activity,
        titleText: String,
        quoteText: String,
        authorText: String
    ): AlertDialog {
        val binding: DialogQuoteBinding = DialogQuoteBinding.inflate(activity.layoutInflater)
        val thanksButton: Button = binding.quoteThanksButton
        val quote: TextView = binding.quoteText
        val author: TextView = binding.quoteAuthor
        val dialogBuilder = AlertDialog.Builder(activity)
        return dialogBuilder.create().apply {
            setView(binding.root)
            quote.text = activity.getString(R.string.quote_text, quoteText)
            author.text = authorText
            thanksButton.setOnClickListener {
                dismiss()
            }
            setTitle(titleText)
        }
    }

    fun buildCustomQuizSizeDialog(
        activity: Activity,
        titleText: String,
        doItClick: (size: Int) -> Unit
    ): AlertDialog {
        val binding = DialogAddQuizSizeBinding.inflate(activity.layoutInflater)
        val startButton: Button = binding.startButton
        val customSize: TextInputEditText = binding.quizSize
        val textLayout = binding.quizSizeLayout
        val dialogBuilder = MaterialAlertDialogBuilder(activity)
        return dialogBuilder.create().apply {
            setView(binding.root)
            startButton.setOnClickListener {
                customSize.text?.let {
                    if (it.isEmpty() || it.toString() == "0") {
                        textLayout.error = activity.getString(R.string.custom_dialog_error)
                    } else {
                        doItClick(it.toString().toInt())
                        dismiss()
                    }
                }
            }

            customSize.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    textLayout.error = null
                }
            })
            setTitle(titleText)
        }
    }
}