package com.vocabulary.myvocabulary.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.display
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.ui.quizzes.toDirectionType
import com.vocabulary.myvocabulary.ui.quizzes.toInt
import kotlinx.android.synthetic.main.fragment_result.view.*
import org.koin.androidx.viewmodel.ext.sharedViewModel
import org.koin.core.parameter.parametersOf

class ResultFragment : Fragment() {
    private val resultViewModel: ResultViewModel by sharedViewModel {
        parametersOf(
                ResultFragmentArgs.fromBundle(arguments!!).dictionaryId
        )
    }
    private var isFabOpen = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        resultViewModel.setDirection(ResultFragmentArgs.fromBundle(arguments!!).directionType.toDirectionType())
        val resultAdapter = ResultAdapter(emptyList(), resultViewModel.directionResult)
        resultViewModel.getGuessResult()
        return inflater.inflate(R.layout.fragment_result, container, false).apply {
            observeWordList(resultAdapter, result_progress_bar)
            generateWordList(resultAdapter, result_recycler_view)
            setExitFabOnClickListener(result_exit_fab)
            setRetryFabOnClickListener(result_restart_fab, failed_only_container, start_over_container)
            setStartOverOnClickListener(start_over_fab)
            setFailedOnlyOnClickListener(failed_only_fab)
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

    private fun setRetryFabOnClickListener(restartFab: FloatingActionButton, failedOnlyContainer: LinearLayout, startOverFabContainer: LinearLayout) {
        restartFab.setOnClickListener {
            if (isFabOpen) closeFabMenu(failedOnlyContainer, startOverFabContainer)
            else showFabMenu(failedOnlyContainer, startOverFabContainer)
        }
    }

    private fun closeFabMenu(failedOnlyContainer: LinearLayout, startOverFabContainer: LinearLayout) {
        isFabOpen = false
        failedOnlyContainer.display(false)
        startOverFabContainer.display(false)
        failedOnlyContainer.animate().translationY(0f)
        startOverFabContainer.animate().translationY(0f)
    }

    private fun showFabMenu(failedOnlyFabContainer: LinearLayout, startOverFabContainer: LinearLayout) {
        isFabOpen = true
        failedOnlyFabContainer.display(!resultViewModel.isAllPassed)
        startOverFabContainer.display(true)
        failedOnlyFabContainer.animate().translationY(-resources.getDimension(R.dimen.standard_150))
        startOverFabContainer.animate().translationY(-resources.getDimension(R.dimen.standard_75))
    }

    private fun setStartOverOnClickListener(startOverFab: FloatingActionButton) {
        startOverFab.setOnClickListener {
            val action = ResultFragmentDirections.fromResultToQuiz(ResultFragmentArgs.fromBundle(arguments!!).dictionaryId, resultViewModel.directionResult.toInt(), false)
            findNavController().navigate(action)
        }
    }

    private fun setFailedOnlyOnClickListener(startOverFab: FloatingActionButton) {
        startOverFab.setOnClickListener {
            val action = ResultFragmentDirections.fromResultToQuiz(ResultFragmentArgs.fromBundle(arguments!!).dictionaryId, resultViewModel.directionResult.toInt(), true)
            findNavController().navigate(action)
        }
    }
}