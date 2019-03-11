package com.vocabulary.myvocabulary.ui.dictionaries

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.room.dictionaryData.DefaultDictionary
import kotlinx.android.synthetic.main.create_dictionary_dialog.view.*
import kotlinx.android.synthetic.main.fragment_dictionary_list.view.*
import kotlinx.android.synthetic.main.rename_dictionary_dialog.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.viewModel

class DictionaryListFragment : Fragment(), DictionaryAdapter.ItemClickListener {
    private val viewModel: DictionaryListViewModel by viewModel()
    private val defaultDictionary: DefaultDictionary by inject()
    private var createDialog: AlertDialog? = null
    private var renameDialog: AlertDialog? = null
    private var popUp: PopupMenu? = null

    override fun onItemClick(dictionary: Dictionary) {
        val action = DictionaryListFragmentDirections.actionDictionaryToWordList(dictionary.dictionaryId)
        findNavController().navigate(action)
    }

    override fun onOptionsClick(dictionary: Dictionary, view: View) {
        createPopUpMenu(dictionary, view)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val dictionaryAdapter = DictionaryAdapter(ArrayList(), this)
        setDefaultDatabase()

        return inflater.inflate(R.layout.fragment_dictionary_list, container, false).apply {
            generateDictionaryList(dictionaryAdapter, this.dictionary_recycler_view)
            observeList(dictionaryAdapter, this.progress_bar)
            setFabOnClickListener(this.dictionary_fab)
        }
    }

    private fun createPopUpMenu(dictionary: Dictionary, view: View) {
        popUp = PopupMenu(requireActivity(), view).apply {
            inflate(R.menu.dictionary_options_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_dictionary_update -> showRenameDialog(dictionary)
                    R.id.menu_dictionary_delete -> showDeleteDialog(dictionary)
                }
                true
            }
            show()
        }
    }

    private fun generateDictionaryList(dictionaryAdapter: DictionaryAdapter, recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = dictionaryAdapter
        }
    }

    private fun observeList(dictionaryAdapter: DictionaryAdapter, progressBar: ProgressBar) {
        progressBar.show(true)
        viewModel.getDictionaryList().observe(this, Observer {
            dictionaryAdapter.updateList(it)
            progressBar.show(false)

        })
    }

    private fun setDefaultDatabase() {
        viewModel.getNumberOfDictionaries().observe(this, Observer {
            if (it == 0) {
                viewModel.insertDictionary(defaultDictionary.getDefaultDictionary())
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
        val dialogView: View = inflater.inflate(R.layout.create_dictionary_dialog, null)
        val editText: EditText = dialogView.new_dictionary_edit
        val createButton: Button = dialogView.create_dictionary_button
        val cancelButton: Button = dialogView.cancel_dictionary_creation
        val errorMessage: TextView = dialogView.dictionary_name_error

        val dialogBuilder = AlertDialog.Builder(requireActivity(), R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
        createDialog = dialogBuilder.create().apply {
            setView(dialogView)
            errorMessage.show(false)
            setupTextChangedListener(editText, errorMessage)
            createButton.setOnClickListener {
                createDictionary(editText.text.toString(), this, errorMessage)
            }
            cancelButton.setOnClickListener {
                errorMessage.show(false)
                dismiss()
            }
            setCanceledOnTouchOutside(false)
            setTitle(R.string.create_new_dictionary_dialog_title)
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

    private fun createDictionary(inputText: String, optionDialog: AlertDialog, errorMessage: TextView) {
        if (!inputText.isEmpty()) {
            errorMessage.show(false)
            viewModel.insertDictionary(viewModel.createDictionaryObject(inputText))
            val action = DictionaryListFragmentDirections.actionDictionaryToWordList(viewModel.getCreatedId())
            this.findNavController().navigate(action)
            optionDialog.dismiss()
        } else {
            errorMessage.show(true)
        }
    }

    override fun onStop() {
        createDialog?.dismiss()
        renameDialog?.dismiss()
        popUp?.dismiss()
        super.onStop()
    }

    private fun showDeleteDialog(dictionary: Dictionary) {
        AlertDialog.Builder(requireActivity(), R.style.ThemeOverlay_MaterialComponents_Dialog_Alert).apply {
            setTitle(R.string.dialog_delete_dictionary_title)
            setMessage("Are you sure you want to delete \"${dictionary.dictionaryName}\" ?")
            setPositiveButton("Delete") { _, _ ->
                viewModel.deleteDictionary(dictionary)
            }
            show()
        }
    }

    private fun showRenameDialog(dictionary: Dictionary) {
        val inflater = requireActivity().layoutInflater
        val dialogView: View = inflater.inflate(R.layout.rename_dictionary_dialog, null)
        val editText: EditText = dialogView.rename_dictionary_edit
        val renameButton: Button = dialogView.rename_dictionary_button
        val cancelButton: Button = dialogView.cancel_dictionary_rename_dialog
        val errorMessage: TextView = dialogView.dictionary_name_error_rename
        val dialogBuilder = AlertDialog.Builder(requireActivity(), R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
        renameDialog = dialogBuilder.create().apply {
            setView(dialogView)
            errorMessage.show(false)
            setupTextChangedListener(editText, errorMessage)
            cancelButton.setOnClickListener {
                errorMessage.show(false)
                dismiss()
            }
            setTitle("Renaming \"${dictionary.dictionaryName}\" dictionary")
            renameButton.setOnClickListener {
                renameDictionary(editText.text.toString(), this, errorMessage, dictionary)
            }
            show()
        }
    }

    private fun renameDictionary(inputText: String, optionDialog: AlertDialog, errorMessage: TextView, dictionary: Dictionary) {
        if (inputText.isNotEmpty()) {
            errorMessage.show(false)
            viewModel.renameDictionary(dictionary.copy(dictionaryName = inputText))
            optionDialog.dismiss()
        } else {
            errorMessage.show(true)
        }
    }
}