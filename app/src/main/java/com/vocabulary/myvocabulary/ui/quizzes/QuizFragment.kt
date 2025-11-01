package com.vocabulary.myvocabulary.ui.quizzes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.ui.results.ResultViewModel
import com.vocabulary.myvocabulary.utils.ItemDecorator
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import com.vocabulary.myvocabulary.databinding.FragmentQuizBinding

class QuizFragment : Fragment() {
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<QuizFragmentArgs>()

    //TODO: Is it ok if it's "by viewModel" and not "by sharedViewModel"?
    private val resultViewModel: ResultViewModel by viewModel {
        parametersOf(
            args.dictionaryId,
            args.quizOption
        )
    }
    private val quizViewModel: QuizViewModel by viewModel {
        parametersOf(
            args.dictionaryId,
            args.quizOption,
            args.failedOnly
        )
    }
    private lateinit var quizAdapter: QuizAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        val view = binding.root

        quizAdapter = QuizAdapter(mutableListOf(), quizViewModel.directionType)
        if (savedInstanceState == null) {
            resultViewModel.resetGuessedWordCollections()
        }
        generateWordList(quizAdapter, binding.quizRecyclerView)
        observeWordList(quizAdapter, binding.quizProgressBar)
        setNextButtonIconUpdateListener(binding.quizNextFab)
        setNextFabOnClickListener(binding.quizNextFab)
        binding.quizToolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        return view
    }

    private fun setNextFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            resultViewModel.latestGuess(quizAdapter.lastGuess())

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
            fab.setImageDrawable(
                ContextCompat
                    .getDrawable(
                        requireContext(),
                        if (it) R.drawable.ic_tick_icon else R.drawable.ic_arrow_right
                    )
            )
        })
    }

    private fun observeWordList(quizAdapter: QuizAdapter, progressBar: ProgressBar) {
        progressBar.show(true)
        quizViewModel.getLiveWordList().observe(requireActivity(), Observer {
            if (!quizViewModel.isDictionaryEmpty) {
                quizAdapter.updateList(it)
                progressBar.show(false)
            } else {
                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.empty_dictionary_notification),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}