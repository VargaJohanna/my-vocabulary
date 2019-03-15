package com.vocabulary.myvocabulary.ui.quizzes

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
import kotlinx.android.synthetic.main.fragment_quiz.view.*
import org.koin.androidx.viewmodel.ext.viewModel
import org.koin.core.parameter.parametersOf

class QuizFragment : Fragment() {
    private val viewModel: QuizViewModel by viewModel {
        parametersOf(QuizFragmentArgs.fromBundle(arguments!!).dictionaryIdForQuiz)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val quizAdapter = QuizAskMeaningAdapter(emptyList())
        return inflater.inflate(R.layout.fragment_quiz, container, false).apply {
            generateWordList(quizAdapter, quiz_recycler_view)
            observeWordList(quizAdapter, quiz_progress_bar)
            setNextFabOnClickListener(quiz_next_fab)
            setCancelFabOnClickListener(quiz_cancel_fab)

        }
    }

    private fun setCancelFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            // Navigate back to quiz list
        }
    }

    private fun setNextFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            viewModel.nextClicked()
        }
    }

    private fun observeWordList(quizAdapter: QuizAskMeaningAdapter, progressBar: ProgressBar) {
        progressBar.show(true)
        viewModel.getLiveWordList().observe(requireActivity(), Observer { list ->
            quizAdapter.updateList(list)
            progressBar.show(false)
        })
    }

    private fun generateWordList(quizAdapter: QuizAskMeaningAdapter, recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = quizAdapter
        }
    }
}