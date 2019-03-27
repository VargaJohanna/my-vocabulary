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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import kotlinx.android.synthetic.main.dialog_create_dictionary.view.*
import kotlinx.android.synthetic.main.dialog_rename_dictionary.view.*
import kotlinx.android.synthetic.main.fragment_dictionary_list.view.*
import org.koin.androidx.viewmodel.ext.viewModel

class DictionaryListFragment : Fragment(), DictionaryAdapter.ItemClickListener {
    private val viewModel: DictionaryListViewModel by viewModel()
    private var createDialog: AlertDialog? = null
    private var renameDialog: AlertDialog? = null
    private var popUp: PopupMenu? = null

    override fun onItemClick(dictionary: Dictionary) {
        val action = DictionaryListFragmentDirections.actionDictionaryToWordList(dictionary.dictionaryId, dictionary.dictionaryName)
        findNavController().navigate(action)
    }

    override fun onOptionsClick(dictionary: Dictionary, view: View) {
        createPopUpMenu(dictionary, view)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val dictionaryAdapter = DictionaryAdapter(ArrayList(), this, true)

        return inflater.inflate(R.layout.fragment_dictionary_list, container, false).apply {
            generateDictionaryList(dictionaryAdapter, dictionary_recycler_view)
            observeList(dictionaryAdapter, progress_bar)
            setFabOnClickListener(dictionary_fab)
            dictionary_list_toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
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

    private fun setFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            openCreateDialog()
        }
    }

    private fun openCreateDialog() {
        val inflater = requireActivity().layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_create_dictionary, null)
        val editText: EditText = dialogView.new_dictionary_edit
        val createButton: Button = dialogView.create_dictionary_button
        val cancelButton: Button = dialogView.cancel_dictionary_creation
        val errorMessage: TextView = dialogView.dictionary_name_error

        val dialogBuilder = AlertDialog.Builder(requireActivity())
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
        if (inputText.isNotEmpty()) {
            errorMessage.show(false)
            viewModel.insertDictionary(viewModel.createDictionaryObject(inputText))
            viewModel.newlyCreatedItemDetails.observe(requireActivity(), Observer { event ->
                event.getContentIfNotHandled()?.let {
                    val action = DictionaryListFragmentDirections.actionDictionaryToWordList(it.dictionaryId, it.dictionaryName)
                    findNavController().navigate(action)
                    optionDialog.dismiss()
                }
            })
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
        AlertDialog.Builder(requireActivity()).apply {
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
        val dialogView: View = inflater.inflate(R.layout.dialog_rename_dictionary, null)
        val editText: EditText = dialogView.rename_dictionary_edit
        val renameButton: Button = dialogView.rename_dictionary_button
        val cancelButton: Button = dialogView.cancel_dictionary_rename_dialog
        val errorMessage: TextView = dialogView.dictionary_name_error_rename
        val dialogBuilder = AlertDialog.Builder(requireActivity())
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