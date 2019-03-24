package com.vocabulary.myvocabulary.ui.quizzes

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryAdapter
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListViewModel
import kotlinx.android.synthetic.main.dialog_direction_option_picker.view.*
import kotlinx.android.synthetic.main.fragment_choose_dictionary.view.*
import org.koin.androidx.viewmodel.ext.viewModel

class DictionaryPickerFragment: Fragment(), DictionaryAdapter.ItemClickListener {
    private val viewModel: DictionaryListViewModel by viewModel()
    private var optionsDialog: AlertDialog? = null
    private val args by navArgs<DictionaryPickerFragmentArgs>()

    override fun onItemClick(dictionary: Dictionary) {
        showOptionsDialog(dictionary.dictionaryId)
    }

    override fun onOptionsClick(dictionary: Dictionary, view: View) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val dictionaryAdapter = DictionaryAdapter(ArrayList(), this, false)

        return inflater.inflate(R.layout.fragment_choose_dictionary, container, false).apply {
            generateDictionaryList(dictionaryAdapter, quiz_dictionary_picker_recycler_view)
            observeList(dictionaryAdapter, progress_bar_dictionary_picker)
        }
    }

    private fun observeList(dictionaryAdapter: DictionaryAdapter, progressBar: ProgressBar) {
        progressBar.show(true)
        viewModel.getDictionaryList().observe(this, Observer {
            dictionaryAdapter.updateList(it)
            progressBar.show(false)
        })
    }

    private fun generateDictionaryList(dictionaryAdapter: DictionaryAdapter, recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = dictionaryAdapter
        }
    }

    private fun showOptionsDialog(dictionaryId: Long) {
        var selectedOption = 0
        val inflater = requireActivity().layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_direction_option_picker, null)
        val radioGroup: RadioGroup = dialogView.radioGroup
        val doItButton: Button = dialogView.lets_do_it_button
        val cancelButton: Button = dialogView.cancel_direction_picker_dialog
        val errorMessage: TextView = dialogView.option_picker_error
        val dialogBuilder = AlertDialog.Builder(requireActivity(), R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
        optionsDialog = dialogBuilder.create().apply {
            setView(dialogView)
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                errorMessage.show(false)
                selectedOption = if (checkedId == R.id.word_radio) 0 else 1
            }
            doItButton.setOnClickListener {
                startQuiz(selectedOption, dictionaryId)
                dismiss()
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
            setTitle(R.string.dialog_pick_direction)
            show()
        }
    }

    private fun startQuiz(selectedOption: Int, dictionaryId: Long) {
        val action = DictionaryPickerFragmentDirections.actionDictionaryPickerFragmentToQuizFragment(
                dictionaryId,
                selectedOption,
                args.quizType
                )
        findNavController().navigate(action)
    }

    override fun onStop() {
        optionsDialog?.dismiss()
        super.onStop()
    }
}