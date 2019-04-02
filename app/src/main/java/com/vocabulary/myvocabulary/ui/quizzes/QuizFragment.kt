package com.vocabulary.myvocabulary.ui.quizzes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.results.ResultViewModel
import com.vocabulary.myvocabulary.utils.ItemDecorator
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_quiz.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.sharedViewModel
import org.koin.androidx.viewmodel.ext.viewModel
import org.koin.core.parameter.parametersOf

class QuizFragment : Fragment() {
    private val args by navArgs<QuizFragmentArgs>()
    private val resultViewModel: ResultViewModel by sharedViewModel {
        parametersOf(
                args.dictionaryId,
                args.quizOption
        )
    }
    private val quizViewModel: QuizViewModel by viewModel {
        parametersOf(
                args.dictionaryId,
                args.quizOption,
                args.failedOnly,
                args.quizType
        )
    }
    private lateinit var quizAdapter:QuizAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        quizAdapter = QuizAdapter(mutableListOf(), quizViewModel.directionType)
        if (savedInstanceState == null) {
            resultViewModel.resetGuessedWordCollections()
        }
        return inflater.inflate(R.layout.fragment_quiz, container, false).apply {
            generateWordList(quizAdapter, quiz_recycler_view)
            observeWordList(quizAdapter, quiz_progress_bar)
            setNextButtonIconUpdateListener(quiz_next_fab)
            setNextFabOnClickListener(quiz_next_fab)
            quiz_toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        }
    }

    private fun setNextFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            if (quizAdapter.lastGuess() != null) {
                resultViewModel.latestGuess(quizAdapter.lastGuess()!!)
            } else {
                Toast.makeText(requireActivity(), resources.getString(R.string.empty_dictionary_notification), Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }

            if (quizViewModel.listIsNotFinished()) {
                quizViewModel.nextClicked()
            } else {
                val action = QuizFragmentDirections.toResultFragment(
                        quizViewModel.dictionaryId,
                        quizViewModel.optionType,
                        args.quizType
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun setNextButtonIconUpdateListener(fab: FloatingActionButton) {
        quizViewModel.getUpdateIcon().observe(requireActivity(), Observer {
            fab.setImageDrawable(resources
                    .getDrawable(if (it) R.drawable.ic_tick_icon else R.drawable.ic_arrow_right, requireActivity().theme))
        })
    }

    private fun observeWordList(quizAdapter: QuizAdapter, progressBar: ProgressBar) {
        progressBar.show(true)
        quizViewModel.getLiveWordList().observe(requireActivity(), Observer {
            quizAdapter.updateList(it)
            progressBar.show(false)
        })
    }

    private fun generateWordList(quizAdapter: QuizAdapter, recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun canScrollVertically(): Boolean = false
            }

            addItemDecoration(ItemDecorator(65))
            adapter = quizAdapter
        }
    }
}