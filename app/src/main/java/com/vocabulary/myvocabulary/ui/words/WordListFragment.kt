package com.vocabulary.myvocabulary.ui.words

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import kotlinx.android.synthetic.main.dialog_create_word.view.*
import kotlinx.android.synthetic.main.dialog_rename_word.view.*
import kotlinx.android.synthetic.main.fragment_word_list.view.*
import org.koin.androidx.viewmodel.ext.viewModel
import org.koin.core.parameter.parametersOf

class WordListFragment : Fragment(), WordAdapter.WordItemClickListener {
    private val args by navArgs<WordListFragmentArgs>()
    private val wordViewModel: WordListViewModel by viewModel {
        parametersOf(args.dictionaryId)
    }
    private var createDialog: AlertDialog? = null
    private var renameDialog: AlertDialog? = null
    private var popUp: PopupMenu? = null

    override fun onItemClick(word: Word) {
        //Open word detail fragment
    }

    override fun onOptionsClick(word: Word, view: View) {
        createPopUpMenu(word, view)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val wordAdapter = WordAdapter(emptyList(), this)
        return inflater.inflate(R.layout.fragment_word_list, container, false).apply {
            generateWordList(wordAdapter, word_recycler_view)
            observeWordList(wordAdapter, word_list_progress_bar)
            setFabOnClickListener(word_fab)
            word_list_toolbar.title = args.dictonaryName
            word_list_toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
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

    private fun setFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            openCreateDialog()
        }
    }

    private fun openCreateDialog() {
        val inflater = requireActivity().layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_create_word, null)
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
            editTextWord.requestFocus()
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
                editTextWord.requestFocus()

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
            editTextWord.requestFocus()
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
        renameDialog?.dismiss()
        popUp?.dismiss()
        super.onStop()
    }

    private fun createPopUpMenu(word: Word, view: View) {
        popUp = PopupMenu(requireActivity(), view).apply {
            inflate(R.menu.word_options_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_word_edit -> showEditDialog(word)
                    R.id.menu_word_delete -> showDeleteWordDialog(word)
                }
                true
            }
            show()
        }
    }

    private fun showDeleteWordDialog(word: Word) {
        AlertDialog.Builder(requireActivity(), R.style.ThemeOverlay_MaterialComponents_Dialog_Alert).apply {
            setTitle(R.string.dialog_delete_word_title)
            setMessage("Are you sure you want to delete\n\"${word.word} - ${word.translation}\" ?")
            setPositiveButton("Delete") { _, _ ->
                wordViewModel.deleteWord(word)
            }
            show()
        }
    }

    private fun showEditDialog(word: Word) {
        val inflater = requireActivity().layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_rename_word, null)
        val editTextWord: EditText = dialogView.rename_word_edit
        val editTextTranslation: EditText = dialogView.rename_translation_edit
        val saveButton: Button = dialogView.rename_and_close_button
        val cancelButton: TextView = dialogView.cancel_word_rename_button
        val errorMessageWord: TextView = dialogView.word_rename_error
        val errorMessageTranslation: TextView = dialogView.rename_translation_error

        val dialogBuilder = AlertDialog.Builder(requireActivity(), R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
        renameDialog = dialogBuilder.create().apply {
            setView(dialogView)
            editTextWord.requestFocus()
            errorMessageWord.show(false)
            errorMessageTranslation.show(false)
            setupTextChangedListener(editTextWord, errorMessageWord)
            setupTextChangedListener(editTextTranslation, errorMessageTranslation)
            editTextWord.setText(word.word)
            editTextTranslation.setText(word.translation)
            saveButton.setOnClickListener {
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

}