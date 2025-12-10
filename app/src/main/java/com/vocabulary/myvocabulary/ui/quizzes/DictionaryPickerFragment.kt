package com.vocabulary.myvocabulary.ui.quizzes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryAdapter
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListViewModel
import com.vocabulary.myvocabulary.utils.DialogFactory
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.vocabulary.myvocabulary.databinding.FragmentChooseDictionaryBinding

class DictionaryPickerFragment : Fragment(), DictionaryAdapter.ItemClickListener {
    private var _binding: FragmentChooseDictionaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DictionaryListViewModel by viewModel()
    private val dialogFactory: DialogFactory by inject()
    private var optionsDialog: AlertDialog? = null
    private val args by navArgs<DictionaryPickerFragmentArgs>()
    private val disposables = CompositeDisposable()

    override fun onItemClick(dictionary: Dictionary) {
        optionsDialog = dialogFactory.buildOptionsDialog(
                requireActivity()
        ) { selectedOption ->
            startQuiz(selectedOption, dictionary.dictionaryId)
        }
        optionsDialog?.show()
        viewModel.setDictionaryTitle(dictionary.dictionaryName)
    }

    override fun onOptionsClick(dictionary: Dictionary, view: View) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChooseDictionaryBinding.inflate(inflater, container, false)
        val view = binding.root
        val dictionaryAdapter = DictionaryAdapter(ArrayList(), this, false)
        generateDictionaryList(dictionaryAdapter, binding.quizDictionaryPickerRecyclerView)
        observeList(dictionaryAdapter, binding.progressBarDictionaryPicker)
        setToolBarTitle(binding.dictionaryPickerToolbar)
        binding.dictionaryPickerToolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        return inflater.inflate(R.layout.fragment_choose_dictionary, container, false).apply {

        }
    }

    private fun observeList(dictionaryAdapter: DictionaryAdapter, progressBar: ProgressBar) {
        progressBar.show(true)
        viewModel.liveDictionaryList.observe(viewLifecycleOwner, Observer {
            dictionaryAdapter.updateList(it)
            progressBar.show(false)
        })
    }

    private fun generateDictionaryList(dictionaryAdapter: DictionaryAdapter, recyclerView: RecyclerView) {
//        recyclerView.apply {
//            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
//            adapter = dictionaryAdapter
//        }
    }

    private fun startQuiz(selectedOption: Int, dictionaryId: Long) {
        disposables += viewModel.startNew(dictionaryId, args.quizType.toQuizType()).subscribe {
            val action = DictionaryPickerFragmentDirections.actionDictionaryPickerFragmentToQuizFragment(
                    dictionaryId,
                    selectedOption,
                    args.quizType
            )
            findNavController().navigate(action)
        }
    }

    private fun setToolBarTitle(toolbar: Toolbar) {
        when (args.quizType.toQuizType()) {
            QuizTypes.QuickQuiz -> toolbar.title = getString(R.string.quiz_list_quick_one)
            QuizTypes.FullQuiz -> toolbar.title = getString(R.string.quiz_list_ask_me_everything)
            QuizTypes.WeakestQuiz -> toolbar.title = getString(R.string.quiz_list_weaknesses)
            QuizTypes.CustomQuiz -> toolbar.title = getString(R.string.quiz_list_custom)
        }
    }

    override fun onDestroy() {
        optionsDialog?.dismiss()
        disposables.clear()
        super.onDestroy()
    }
}