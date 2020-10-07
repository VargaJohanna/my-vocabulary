package com.vocabulary.myvocabulary.ui.dictionaries

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.display
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortByDictionaryOptions
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.toQuizType
import com.vocabulary.myvocabulary.utils.DialogFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dictionary_list.*
import kotlinx.android.synthetic.main.fragment_dictionary_list.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.viewModel

class DictionaryListFragment : Fragment(), DictionaryAdapter.ItemClickListener {
    private val viewModel: DictionaryListViewModel by viewModel()
    private val shareViewModel: ShareDictionaryViewModel by viewModel()
    private val dialogFactory: DialogFactory by inject()
    private val rxSchedulers: RxSchedulers by inject()
    private val disposables = CompositeDisposable()
    private var createDialog: AlertDialog? = null
    private var renameDialog: AlertDialog? = null
    private var startQuizDialog: AlertDialog? = null
    private var popUp: PopupMenu? = null
    private var importDialog: AlertDialog? = null
    private var isFabOpen = false

    override fun onItemClick(dictionary: Dictionary) {
        val action = DictionaryListFragmentDirections.actionDictionaryToWordList(dictionary.dictionaryId, dictionary.dictionaryName)
        findNavController().navigate(action)
    }

    override fun onOptionsClick(dictionary: Dictionary, view: View) {
        createPopUpMenu(dictionary, view)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val dictionaryAdapter = DictionaryAdapter(ArrayList(), this, true)
        shareViewModel.fetchCsvUri()

        return inflater.inflate(R.layout.fragment_dictionary_list, container, false).apply {
            generateDictionaryList(dictionaryAdapter, dictionary_recycler_view)
            observeList(dictionaryAdapter, progress_bar)
            setFabOnClickListener(dictionary_fab, import_container, create_new_container)
            setCreateNewFabOnClickListener(create_new_fab)
            setImportFabOnClickListener(import_fab)
            dictionary_list_toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
            viewModel.isListEmpty().observe(this@DictionaryListFragment, Observer {
                inflateToolbarMenu(it, dictionary_list_toolbar)
            })
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

    private fun setFabOnClickListener(fab: FloatingActionButton, importContainer: LinearLayout, createNewContainer: LinearLayout) {
        fab.setOnClickListener {
            if (isFabOpen) closeFabMenu(importContainer, createNewContainer)
            else showFabMenu(importContainer, createNewContainer)
        }
    }

    private fun setCreateNewFabOnClickListener(createFab: FloatingActionButton) {
        createFab.setOnClickListener {
            createDialog = dialogFactory.buildDictionaryCreateDialog(
                    requireActivity(),
                    getString(R.string.create_new_dictionary_dialog_title)
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
            closeFabMenu(import_container, create_new_container)
            createDialog?.show()
        }
    }

    private fun setImportFabOnClickListener(importFab: FloatingActionButton) {
        importFab.setOnClickListener {
            requestFile()
            closeFabMenu(import_container, create_new_container)
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
                dictionary.dictionaryName) { selectedDirection: Int, id: Long, selectedQuizType: Int, customSize: Int? ->
            viewModel.addCustomQuizSize(customSize)
            startQuiz(selectedDirection, id, selectedQuizType)
            startQuizDialog?.dismiss()
        }
        startQuizDialog?.show()
    }

    private fun startQuiz(selectedOption: Int, dictionaryId: Long, selectedQuiz: Int) {
        disposables += viewModel.startNew(dictionaryId, selectedQuiz.toQuizType())
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe {
                    val action = DictionaryListFragmentDirections.fromDictionaryToQuiz(
                            dictionaryId,
                            selectedOption,
                            selectedQuiz
                    )
                    findNavController().navigate(action)
                }
    }

    private fun requestFile() {
        val intent = Intent(Intent.ACTION_PICK)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(intent, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == RESULT_OK && intent != null) {
            if (intent.data != null) {
                if (requireActivity().contentResolver.getType(intent.data!!) == "text/csv") {
                    shareViewModel.saveCsvData(intent.data!!)
                    shareViewModel.setIsImport(true)
                } else {
                    shareViewModel.setIsImport(false)
                    Toast.makeText(requireActivity(), getString(R.string.only_csv_import), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun closeFabMenu(importContainer: LinearLayout, createNewContainer: LinearLayout) {
        isFabOpen = false
        importContainer.display(false)
        createNewContainer.display(false)
        importContainer.animate().translationY(0f)
        createNewContainer.animate().translationY(0f)
    }

    private fun showFabMenu(importContainer: LinearLayout, createNewContainer: LinearLayout) {
        isFabOpen = true
        importContainer.display(true)
        createNewContainer.display(true)
        importContainer.animate().translationY(-resources.getDimension(R.dimen.standard_150))
        createNewContainer.animate().translationY(-resources.getDimension(R.dimen.standard_75))
    }

    private fun inflateToolbarMenu(isListEmpty: Boolean, toolbar: Toolbar) {
        toolbar.apply {
            if (!isListEmpty && toolbar.menu.size() == 0) {
                inflateMenu(R.menu.dictionary_list_menu)
                navigationIconsSet(menu.getItem(0).subMenu)
                setOnMenuItemClickListener { item: MenuItem? ->
                    navigationIconsSet(menu.getItem(0).subMenu)
                    when (item?.itemId) {
                        R.id.dictionary_sort_by_date -> {
                            viewModel.setSortBy(viewModel.currentSortByData.copy(
                                    sortByOption = SortByDictionaryOptions.SortByDate,
                                    dateDescending = !viewModel.currentSortByData.dateDescending)
                            )
                        }
                        R.id.dictionary_sort_by_title -> {
                            viewModel.setSortBy(viewModel.currentSortByData.copy(
                                    sortByOption = SortByDictionaryOptions.SortByTitle,
                                    titleDescending = !viewModel.currentSortByData.titleDescending)
                            )
                        }
                    }
                    true
                }
            } else if (isListEmpty) {
                toolbar.menu.clear()
            }
        }
    }

    private fun navigationIconsSet(menu: Menu) {
        viewModel.currentSortByData.let {
            when (it.sortByOption) {
                SortByDictionaryOptions.SortByDate -> setIcons(menu, R.id.dictionary_sort_by_date, it.dateDescending)
                SortByDictionaryOptions.SortByTitle -> setIcons(menu, R.id.dictionary_sort_by_title, it.titleDescending)
            }
        }
    }

    private fun setIcons(menu: Menu, menuItemId: Int, descending: Boolean) {
        menu.forEach {
            if (it.itemId == menuItemId) it.setIcon(
                    if (descending) R.drawable.ic_arrow_downward
                    else R.drawable.ic_arrow_upward)
            else it.setIcon(R.drawable.ic_empty_icon)
        }
    }

    override fun onDestroy() {
        disposables.clear()
        createDialog?.dismiss()
        renameDialog?.dismiss()
        startQuizDialog?.dismiss()
        popUp?.dismiss()
        importDialog?.dismiss()
        super.onDestroy()
    }
}