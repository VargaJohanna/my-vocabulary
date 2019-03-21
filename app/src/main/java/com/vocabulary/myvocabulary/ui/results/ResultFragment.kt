package com.vocabulary.myvocabulary.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.ui.quizzes.toDirectionType
import kotlinx.android.synthetic.main.fragment_result.view.*
import org.koin.androidx.viewmodel.ext.sharedViewModel
import org.koin.core.parameter.parametersOf

class ResultFragment : Fragment() {
    private val resultViewModel: ResultViewModel by sharedViewModel {
        parametersOf(
                ResultFragmentArgs.fromBundle(arguments!!).dictionaryIdForResult
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        resultViewModel.setDirection(ResultFragmentArgs.fromBundle(arguments!!).directionType.toDirectionType())
        val resultAdapter = ResultAdapter(emptyList(), resultViewModel.directionResult)
        resultViewModel.getGuessResult()
        return inflater.inflate(R.layout.fragment_result, container, false).apply {
            observeWordList(resultAdapter, result_progress_bar)
            generateWordList(resultAdapter, result_recycler_view)
            setExitFabOnClickListener(result_exit_fab)
        }
    }

    private fun generateWordList(resultAdapter: ResultAdapter, recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = resultAdapter
        }
    }

    private fun observeWordList(resultAdapter: ResultAdapter, progressBar: ProgressBar) {
        progressBar.show(true)
        resultViewModel.getLiveGuessedList().observe(requireActivity(), Observer {
            resultAdapter.updateList(it)
            progressBar.show(false)
        })
    }

    private fun setExitFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            findNavController().navigate(R.id.from_result_to_quizListFragment)
        }
    }
}