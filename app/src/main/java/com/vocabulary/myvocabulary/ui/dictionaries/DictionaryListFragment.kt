package com.vocabulary.myvocabulary.ui.dictionaries

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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.room.dictionaryData.DefaultDictionary
import kotlinx.android.synthetic.main.fragment_dictionary_list.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.viewModel

class DictionaryListFragment : Fragment(), DictionaryAdapter.ItemClickListener {
    private val viewModel: DictionaryListViewModel by viewModel()
    private val defaultDictionary: DefaultDictionary by inject()
    private lateinit var dialog: AlertDialog

    override fun onItemClick(dictionary: Dictionary, view: View) {
        val action = DictionaryListFragmentDirections.actionDictionaryToWordList(dictionary.dictionaryId)
        view.findNavController().navigate(action)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val dictionaryAdapter = DictionaryAdapter(ArrayList(), this)
        val dialogBuilder = AlertDialog.Builder(requireActivity(), R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
        return inflater.inflate(R.layout.fragment_dictionary_list, container, false).apply {
            generateDictionaryList(dictionaryAdapter, this.dictionary_recycler_view)
            setDefaultDatabase()
            observeList(dictionaryAdapter, this.progress_bar)
            setFabOnClickListener(this.dictionary_fab)
            dialog = dialogBuilder.create()
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
            createDialog()
        }
    }

    private fun createDialog() {
        val inflater = requireActivity().layoutInflater
        val dialogView: View = inflater.inflate(R.layout.create_dictionary_dialog, null)
        val editText: EditText = dialogView.findViewById(R.id.new_dictionary_title)
        val createButton: Button = dialogView.findViewById(R.id.create_dictionary_button)
        val cancelButton: Button = dialogView.findViewById(R.id.cancel_dictionary_creation)
        val errorMessage: TextView = dialogView.findViewById(R.id.dictionary_name_error)

        dialog.setView(dialogView)
        errorMessage.show(false)
        setupTextChangedListener(editText, errorMessage)
        createButton.setOnClickListener {
            createDictionary(editText.text.toString(), dialog, errorMessage)
        }
        cancelButton.setOnClickListener {
            errorMessage.show(false)
            dialog.dismiss()
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.setTitle(R.string.create_new_dictionary_dialog_title)
        dialog.show()
    }

    private fun setupTextChangedListener(editText: EditText, errorMessage: TextView) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (errorMessage.visibility == View.VISIBLE) errorMessage.show(false)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (errorMessage.visibility == View.VISIBLE) errorMessage.show(false)
            }

            override fun afterTextChanged(p0: Editable?) {
                if (errorMessage.visibility == View.VISIBLE) errorMessage.show(false)
            }
        })
    }

    private fun createDictionary(inputText: String, optionDialog: AlertDialog, errorMessage: TextView) {
        if (!inputText.isEmpty()) {
            errorMessage.show(false)
            viewModel.insertDictionary(viewModel.createDictionary(inputText))
            val action = DictionaryListFragmentDirections.actionDictionaryToWordList(viewModel.getCreatedId())
            this.findNavController().navigate(action)
            optionDialog.dismiss()
        } else {
            errorMessage.show(true)
        }
    }

    override fun onStop() {
        dialog.dismiss()
        super.onStop()
    }
}