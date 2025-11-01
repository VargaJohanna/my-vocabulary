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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.display
import com.vocabulary.myvocabulary.ext.fadeoutAnimation
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.toDirectionType
import com.vocabulary.myvocabulary.ui.quizzes.toInt
import com.vocabulary.myvocabulary.ui.quizzes.toQuizType
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.round
import com.vocabulary.myvocabulary.databinding.FragmentResultBinding

class ResultFragment : Fragment() {
    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<ResultFragmentArgs>()

    //TODO: is it ok to use "by viewModel" instead of "by sharedViewModel"
    private val resultViewModel: ResultViewModel by viewModel() {
        parametersOf(
            args.dictionaryId
        )
    }
    private val rxSchedulers: RxSchedulers by inject()
    private var isFabOpen = false
    private val disposables = CompositeDisposable()
    private lateinit var manager: ReviewManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        val view = binding.root

        manager = ReviewManagerFactory.create(requireContext())
        hideKeyboard()
        resultViewModel.observeGuessedWordMap()
        resultViewModel.setDirection(args.directionType.toDirectionType())
        val resultAdapter = ResultAdapter(emptyList(), resultViewModel.directionResult)

        observeWordList(
            resultAdapter,
            binding.resultProgressBar,
            binding.successAnimation,
            binding.failureAnimation,
            savedInstanceState
        )
        generateWordList(resultAdapter, binding.resultRecyclerView)
        setExitFabOnClickListener(binding.resultExitFab)
        setStartOverOnClickListener(binding.startOverFab)
        setFailedOnlyOnClickListener(binding.failedOnlyFab)
        binding.resultToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
            resultViewModel.dispose()
        }
        closeAnimationOnClick(binding.successAnimation)
        closeAnimationOnClick(binding.failureAnimation)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setRetryFabOnClickListener()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun generateWordList(resultAdapter: ResultAdapter, recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = resultAdapter
        }
    }

    private fun observeWordList(
        resultAdapter: ResultAdapter,
        progressBar: ProgressBar,
        successAnimation: LottieAnimationView,
        failureAnimation: LottieAnimationView,
        savedInstanceState: Bundle?
    ) {
        progressBar.show(true)
        resultViewModel.getLiveGuessedList().observe(requireActivity(), Observer {
            resultAdapter.updateList(it)
            progressBar.show(false)
            if (it.isNotEmpty()) setResultStatistics(it)
            if (savedInstanceState == null && it.isNotEmpty()) {
                showAnimation(successAnimation, failureAnimation)
            }
        })
    }

    private fun setResultStatistics(list: List<Word>) {
        val passes = list.filter { it.lastResult }.size
        val all = list.size
        binding.resultStats.text = String.format(
            getString(R.string.result_stats),
            passes,
            all,
            round(((passes.toFloat() / all.toFloat()) * 100)).toInt()
        )
    }

    private fun setExitFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            showInAppReview()
            resultViewModel.dispose()
            findNavController().navigate(R.id.from_result_to_home)
        }
    }

    private fun setRetryFabOnClickListener() {
        binding.resultRestartFab.setOnClickListener {
            showInAppReview()
            if (isFabOpen) closeFabMenu(binding.failedOnlyContainer, binding.startOverContainer)
            else showFabMenu(binding.failedOnlyContainer, binding.startOverContainer)
        }
    }

    private fun closeFabMenu(
        failedOnlyContainer: LinearLayout, startOverFabContainer: LinearLayout
    ) {
        isFabOpen = false
        failedOnlyContainer.display(false)
        startOverFabContainer.display(false)
        failedOnlyContainer.animate().translationY(0f)
        startOverFabContainer.animate().translationY(0f)
    }

    private fun showFabMenu(
        failedOnlyFabContainer: LinearLayout, startOverFabContainer: LinearLayout
    ) {
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
                .subscribeOn(rxSchedulers.io()).observeOn(rxSchedulers.main()).subscribe {
                    val action = ResultFragmentDirections.fromResultToQuiz(
                        args.dictionaryId,
                        resultViewModel.directionResult.toInt(),
                        false,
                        args.quizType
                    )
                    findNavController().navigate(action)
                }
            resultViewModel.dispose()
        }
    }

    private fun setFailedOnlyOnClickListener(startOverFab: FloatingActionButton) {
        startOverFab.setOnClickListener {
            val action = ResultFragmentDirections.fromResultToQuiz(
                args.dictionaryId, resultViewModel.directionResult.toInt(), true, args.quizType
            )
            resultViewModel.dispose()
            findNavController().navigate(action)
        }
    }

    private fun showAnimation(
        animationViewSuccess: LottieAnimationView, animationViewFailure: LottieAnimationView
    ) {
        if (resultViewModel.isAllPassed) {
            animationViewSuccess.show(true)
            animationViewSuccess.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(p0: Animator) {
                    animationViewSuccess.show(false)
                }

                override fun onAnimationRepeat(p0: Animator) {}

                override fun onAnimationCancel(p0: Animator) {}

                override fun onAnimationStart(p0: Animator) {}
            })
        } else {
            animationViewFailure.show(true)
            animationViewFailure.setMinAndMaxFrame(200, 260)
            animationViewFailure.speed = 1.5f
            animationViewFailure.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(p0: Animator) {
                    animationViewFailure.fadeoutAnimation()
                    animationViewFailure.show(false)
                }

                override fun onAnimationRepeat(p0: Animator) {}

                override fun onAnimationCancel(p0: Animator) {}

                override fun onAnimationStart(p0: Animator) {}
            })
        }
    }

    private fun closeAnimationOnClick(animation: LottieAnimationView?) {
        animation?.let { animationView ->
            animationView.setOnClickListener {
                animation.display(false)
                animationView.cancelAnimation()
            }
        }
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view: View? = requireActivity().currentFocus
        if (view == null) View(requireActivity())
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showInAppReview() {
        if (resultViewModel.openedAppCounter > 3) {
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    val reviewInfo = request.result
                    val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                    flow.addOnCompleteListener { _ ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                    }
                }
            }
        }
    }

    override fun onStop() {
        resultViewModel.dispose()
        disposables.clear()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}