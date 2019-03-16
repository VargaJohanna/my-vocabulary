package com.vocabulary.myvocabulary.ui.quizzes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.utils.ItemDecorator
import kotlinx.android.synthetic.main.fragment_quiz.view.*
import org.koin.androidx.viewmodel.ext.viewModel
import org.koin.core.parameter.parametersOf

class QuizFragment : Fragment() {
    private val viewModel: QuizViewModel by viewModel {
        parametersOf(
                QuizFragmentArgs.fromBundle(arguments!!).dictionaryIdForQuiz,
                QuizFragmentArgs.fromBundle(arguments!!).quizOption
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val quizWithMeaningAdapter = QuizAdapter(emptyList(), viewModel.isMeaning())
        return inflater.inflate(R.layout.fragment_quiz, container, false).apply {
            generateWordList(quizWithMeaningAdapter, quiz_recycler_view)
            observeWordList(quizWithMeaningAdapter, quiz_progress_bar)
            setNextButtonIconUpdateListener(quiz_next_fab)
            setNextFabOnClickListener(quiz_next_fab)
        }
    }

    private fun setNextFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            if(viewModel.getListIsFinished().not()) {
                viewModel.nextClicked()
            } else {
                // Open result
                Toast.makeText(requireActivity(), "Open Results", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setNextButtonIconUpdateListener(fab: FloatingActionButton) {
        viewModel.getUpdateIcon().observe(requireActivity(), Observer {
            if(it) {
                fab.setImageDrawable(resources.getDrawable(R.drawable.ic_tick_icon, requireActivity().theme))
            } else {
                fab.setImageDrawable(resources.getDrawable(R.drawable.ic_arrow_right, requireActivity().theme))
            }
        })
    }

    private fun observeWordList(quizAdapter: QuizAdapter, progressBar: ProgressBar) {
        progressBar.show(true)
        viewModel.getLiveWordList().observe(requireActivity(), Observer { list ->
            quizAdapter.updateList(list)
            progressBar.show(false)
        })
    }

    private fun generateWordList(quizAdapter: QuizAdapter, recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(ItemDecorator(-120))
            adapter = quizAdapter
        }
    }
}