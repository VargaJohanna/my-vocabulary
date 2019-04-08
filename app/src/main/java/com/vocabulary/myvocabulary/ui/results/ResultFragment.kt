package com.vocabulary.myvocabulary.ui.results

import android.animation.Animator
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.display
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.toDirectionType
import com.vocabulary.myvocabulary.ui.quizzes.toInt
import com.vocabulary.myvocabulary.ui.quizzes.toQuizType
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_result.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.sharedViewModel
import org.koin.core.parameter.parametersOf

class ResultFragment : Fragment() {
    private val args by navArgs<ResultFragmentArgs>()
    private val resultViewModel: ResultViewModel by sharedViewModel {
        parametersOf(
                args.dictionaryId
        )
    }
    private val rxSchedulers: RxSchedulers by inject()
    private var isFabOpen = false
    private val disposables = CompositeDisposable()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        hideKeyboard()
        resultViewModel.setDirection(args.directionType.toDirectionType())
        val resultAdapter = ResultAdapter(emptyList(), resultViewModel.directionResult)
        resultViewModel.getGuessResult()
        return inflater.inflate(R.layout.fragment_result, container, false).apply {
            observeWordList(resultAdapter, result_progress_bar, success_animation, savedInstanceState)
            generateWordList(resultAdapter, result_recycler_view)
            setExitFabOnClickListener(result_exit_fab)
            setRetryFabOnClickListener(result_restart_fab, failed_only_container, start_over_container)
            setStartOverOnClickListener(start_over_fab)
            setFailedOnlyOnClickListener(failed_only_fab)
            result_toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        }
    }

    private fun generateWordList(resultAdapter: ResultAdapter, recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = resultAdapter
        }
    }

    private fun observeWordList(resultAdapter: ResultAdapter, progressBar: ProgressBar, success_animation: LottieAnimationView, savedInstanceState: Bundle?) {
        progressBar.show(true)
        resultViewModel.getLiveGuessedList().observe(requireActivity(), Observer {
            resultAdapter.updateList(it)
            progressBar.show(false)
            if (savedInstanceState == null && it.isNotEmpty()) {
                showSuccessAnimation(success_animation)
            }
        })
    }

    private fun setExitFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            findNavController().navigate(R.id.from_result_to_home)
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
            resultViewModel.resetGuessedWordCollections()
            disposables += resultViewModel.startNew(args.dictionaryId, args.quizType.toQuizType())
                    .subscribeOn(rxSchedulers.io())
                    .observeOn(rxSchedulers.main())
                    .subscribe {
                val action = ResultFragmentDirections.fromResultToQuiz(
                        args.dictionaryId,
                        resultViewModel.directionResult.toInt(),
                        false,
                        args.quizType
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun setFailedOnlyOnClickListener(startOverFab: FloatingActionButton) {
        startOverFab.setOnClickListener {
            val action = ResultFragmentDirections.fromResultToQuiz(
                    args.dictionaryId,
                    resultViewModel.directionResult.toInt(),
                    true,
                    args.quizType
            )
            findNavController().navigate(action)
        }
    }

    private fun showSuccessAnimation(animationView: LottieAnimationView) {
        if (resultViewModel.isAllPassed) {
            animationView.show(true)
            animationView.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    animationView.show(false)
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                }

            })
        }
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view: View? = requireActivity().currentFocus
        if (view == null) View(requireActivity())
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }
}