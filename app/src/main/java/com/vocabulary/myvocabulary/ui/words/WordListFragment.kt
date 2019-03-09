package com.vocabulary.myvocabulary.ui.words

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
import com.vocabulary.myvocabulary.room.wordData.DefaultWordList
import kotlinx.android.synthetic.main.fragment_word_list.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.viewModel
import org.koin.core.parameter.parametersOf

class WordListFragment : Fragment(), WordAdapter.WordItemClickListener {
    private val defaultWordList: DefaultWordList by inject()
    private val wordViewModel: WordListViewModel by viewModel {
        parametersOf(WordListFragmentArgs.fromBundle(arguments!!).dictionaryId)
    }

    override fun onItemClick(word: Word) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val wordAdapter = WordAdapter(ArrayList(), this)
        return inflater.inflate(R.layout.fragment_word_list, container, false).apply {
            generateWordList(wordAdapter, this.word_recycler_view)
            setDefaultWordDatabase()
            observeWordList(wordAdapter, this.word_list_progress_bar)
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
            if (wordViewModel.getDictionaryId() == 1L && !isWordSet) {
                val wordToAdd = defaultWordList.getDefaultWordList()
                wordToAdd.forEach {
                    wordViewModel.insertWord(it)
                }
            }
        })

    }

}