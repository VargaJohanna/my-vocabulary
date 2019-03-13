package com.vocabulary.myvocabulary.ui.quizzes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryAdapter
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListViewModel
import kotlinx.android.synthetic.main.fragment_choose_dictionary.view.*
import org.koin.androidx.viewmodel.ext.viewModel

class DictionaryPickerFragment: Fragment(), DictionaryAdapter.ItemClickListener {
    private val viewModel: DictionaryListViewModel by viewModel()

    override fun onItemClick(dictionary: Dictionary) {
        // Open quiz fragment
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
}