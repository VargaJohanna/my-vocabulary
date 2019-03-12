package com.vocabulary.myvocabulary.ui.words

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.room.wordData.DefaultWordList
import kotlinx.android.synthetic.main.create_word_dialog.view.*
import kotlinx.android.synthetic.main.fragment_word_list.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.viewModel
import org.koin.core.parameter.parametersOf

class WordListFragment : Fragment(), WordAdapter.WordItemClickListener {
    private val defaultWordList: DefaultWordList by inject()
    private val wordViewModel: WordListViewModel by viewModel {
        parametersOf(WordListFragmentArgs.fromBundle(arguments!!).dictionaryId)
    }
    private var createDialog: AlertDialog? = null

    override fun onItemClick(word: Word) {
        //
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val wordAdapter = WordAdapter(emptyList(), this)
        return inflater.inflate(R.layout.fragment_word_list, container, false).apply {
            generateWordList(wordAdapter, word_recycler_view)
            setDefaultWordDatabase()
            observeWordList(wordAdapter, word_list_progress_bar)
            setFabOnClickListener(word_fab)
            word_list_title.text = WordListFragmentArgs.fromBundle(arguments!!).dictonaryName
        }
    }

    private fun generateWordList(wordAdapter: WordAdapter, recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = wordAdapter
        }
    }

    private fun observeWordList(wordAdapter: WordAdapter, progressBar: ProgressBar) {
        progressBar.show(true)
        wordViewModel.getLiveWordList().observe(requireActivity(), Observer {
            wordAdapter.updateList(it)
            progressBar.show(false)
        })
    }

    private fun setDefaultWordDatabase() {
        wordViewModel.isDefaultWordSet().observe(requireActivity(), Observer { isWordSet ->
            if (wordViewModel.dictionaryId == 1L && !isWordSet) {
                val wordToAdd = defaultWordList.getDefaultWordList()
                wordToAdd.forEach {
                    wordViewModel.insertWord(it)
                }
            }
        })
    }

    private fun setFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            openCreateDialog()
        }
    }

    private fun openCreateDialog() {
        val inflater = requireActivity().layoutInflater
        val dialogView: View = inflater.inflate(R.layout.create_word_dialog, null)
        val editTextWord: EditText = dialogView.new_word_edit
        val editTextTranslation: EditText = dialogView.new_translation_edit
        val saveButton: Button = dialogView.create_and_close_button
        val addMoreButton: Button = dialogView.create_and_keep_adding_button
        val cancelButton: TextView = dialogView.cancel_word_adding_button
        val errorMessageWord: TextView = dialogView.word_name_error
        val errorMessageTranslation: TextView = dialogView.word_translation_error

        val dialogBuilder = AlertDialog.Builder(requireActivity(), R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
        createDialog = dialogBuilder.create().apply {
            setView(dialogView)
            errorMessageWord.show(false)
            errorMessageTranslation.show(false)
            setupTextChangedListener(editTextWord, errorMessageWord)
            setupTextChangedListener(editTextTranslation, errorMessageTranslation)
            saveButton.setOnClickListener {
                createWord(editTextWord,
                        editTextTranslation,
                        errorMessageWord,
                        errorMessageTranslation,
                        true,
                        this)
            }
            addMoreButton.setOnClickListener {
                createWord(editTextWord,
                        editTextTranslation,
                        errorMessageWord,
                        errorMessageTranslation,
                        false,
                        this)

            }

            cancelButton.setOnClickListener {
                dismiss()
            }
            setTitle(R.string.create_new_word_dialog_title)
            show()
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

    private fun createWord(editTextWord: EditText,
                           editTextTranslation: EditText,
                           errorMessageWord: TextView,
                           errorMessageTranslation: TextView,
                           toClose: Boolean,
                           alertDialog: AlertDialog) {
        val inputWord = editTextWord.text.toString().trim()
        val inputTranslation = editTextTranslation.text.toString().trim()

        if (inputWord.isNotEmpty() && inputTranslation.isNotEmpty()) {
            errorMessageWord.show(false)
            errorMessageTranslation.show(false)
            wordViewModel.insertWord(wordViewModel.createWordObject(inputWord, inputTranslation))
            editTextWord.setText("")
            editTextTranslation.setText("")
            if (toClose) {
                alertDialog.dismiss()
            }
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

    override fun onStop() {
        createDialog?.dismiss()
        super.onStop()
    }

}