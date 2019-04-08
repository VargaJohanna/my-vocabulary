package com.vocabulary.myvocabulary.ui.dictionaries

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.toQuizType
import com.vocabulary.myvocabulary.utils.DialogFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dictionary_list.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.viewModel

class DictionaryListFragment : Fragment(), DictionaryAdapter.ItemClickListener {
    private val viewModel: DictionaryListViewModel by viewModel()
    private val dialogFactory: DialogFactory by inject()
    private val rxSchedulers: RxSchedulers by inject()
    private var createDialog: AlertDialog? = null
    private var renameDialog: AlertDialog? = null
    private var startQuizDialog: AlertDialog? = null
    private var popUp: PopupMenu? = null
    private val disposables = CompositeDisposable()

    override fun onItemClick(dictionary: Dictionary) {
        val action = DictionaryListFragmentDirections.actionDictionaryToWordList(dictionary.dictionaryId, dictionary.dictionaryName)
        findNavController().navigate(action)
    }

    override fun onOptionsClick(dictionary: Dictionary, view: View) {
        createPopUpMenu(dictionary, view)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val dictionaryAdapter = DictionaryAdapter(ArrayList(), this, true)

        return inflater.inflate(R.layout.fragment_dictionary_list, container, false).apply {
            generateDictionaryList(dictionaryAdapter, dictionary_recycler_view)
            observeList(dictionaryAdapter, progress_bar)
            setFabOnClickListener(dictionary_fab)
            dictionary_list_toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        }
    }

    private fun generateDictionaryList(dictionaryAdapter: DictionaryAdapter, recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = dictionaryAdapter
        }
    }

    private fun observeList(dictionaryAdapter: DictionaryAdapter, progressBar: ProgressBar) {
        progressBar.show(true)
        viewModel.liveDictionaryList.observe(this, Observer {
            dictionaryAdapter.updateList(it)
            progressBar.show(false)

        })
    }

    private fun setFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            createDialog = dialogFactory.buildDictionaryCreateDialog(
                    requireActivity()
            ) { nameToCreate ->
                viewModel.insertDictionary(viewModel.createDictionaryObject(nameToCreate))
                viewModel.newlyCreatedItemDetails.observe(requireActivity(), Observer { event ->
                    event.getContentIfNotHandled()?.let {
                        val action = DictionaryListFragmentDirections.actionDictionaryToWordList(it.dictionaryId, it.dictionaryName)
                        findNavController().navigate(action)
                        createDialog?.dismiss()
                    }
                })
            }
            createDialog?.show()
        }
    }


    private fun createPopUpMenu(dictionary: Dictionary, view: View) {
        popUp = PopupMenu(requireActivity(), view).apply {
            inflate(R.menu.dictionary_options_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_dictionary_update -> showRenameDialog(dictionary)
                    R.id.menu_dictionary_delete -> showDeleteDialog(dictionary)
                    R.id.menu_dictionary_start_quiz -> showStartQuizDialog(dictionary)
                }
                true
            }
            show()
        }
    }

    private fun showRenameDialog(dictionary: Dictionary) {
        renameDialog = dialogFactory.buildDictionaryRenameDialog(
                requireActivity(),
                dictionary) { renameTo ->
            viewModel.renameDictionary(dictionary.copy(dictionaryName = renameTo))
            renameDialog?.dismiss()
        }
        renameDialog?.show()
    }

    private fun showDeleteDialog(dictionary: Dictionary) {
        dialogFactory.buildDeleteWordDialog(
                requireActivity(),
                getString(R.string.dialog_delete_dictionary_title),
                "${getString(R.string.verify_deletion)} \"${dictionary.dictionaryName}\" ?"
        ) {
            viewModel.deleteDictionary(dictionary)
        }.show()
    }

    private fun showStartQuizDialog(dictionary: Dictionary) {
        startQuizDialog = dialogFactory.buildStartQuizDialog(
                dictionary.dictionaryId,
                requireActivity(),
                dictionary.dictionaryName) { selectedDirection: Int, id: Long, selectedQuizType: Int ->
            startQuiz(selectedDirection, id, selectedQuizType)
            startQuizDialog?.dismiss()
        }
        startQuizDialog?.show()
    }

    private fun startQuiz(selectedOption: Int, dictionaryId: Long, selectedQuiz: Int) {
        disposables += viewModel.startNew(dictionaryId, selectedQuiz.toQuizType())
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe{
            val action = DictionaryListFragmentDirections.fromDictionaryToQuiz(
                    dictionaryId,
                    selectedOption,
                    selectedQuiz
            )
            findNavController().navigate(action)
        }
    }

    override fun onStop() {
        disposables.clear()
        createDialog?.dismiss()
        renameDialog?.dismiss()
        startQuizDialog?.dismiss()
        popUp?.dismiss()
        super.onStop()
    }
}