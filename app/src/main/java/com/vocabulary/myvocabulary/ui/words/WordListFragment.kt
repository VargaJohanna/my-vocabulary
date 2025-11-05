package com.vocabulary.myvocabulary.ui.words

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.display
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.repositories.sortBy.SortByOptions
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.dictionaries.ShareDictionaryViewModel
import com.vocabulary.myvocabulary.ui.quizzes.toQuizType
import com.vocabulary.myvocabulary.utils.DialogFactory
import com.xwray.groupie.GroupAdapter
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import com.xwray.groupie.Item
import com.xwray.groupie.GroupieViewHolder
import com.vocabulary.myvocabulary.databinding.FragmentWordListBinding

class WordListFragment : Fragment() {
    private var _binding: FragmentWordListBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<WordListFragmentArgs>()
    private val wordViewModel: WordListViewModel by viewModel {
        parametersOf(args.dictionaryId)
    }
    private val shareViewModel: ShareDictionaryViewModel by viewModel()
    private val dialogFactory: DialogFactory by inject()
    private val rxSchedulers: RxSchedulers by inject()
    private var createDialog: AlertDialog? = null
    private var startQuizDialog: AlertDialog? = null
    private var searchBar: ConstraintLayout? = null
    private var searchField: EditText? = null
    private val disposables = CompositeDisposable()
    private val wordAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentWordListBinding.inflate(inflater, container, false)
        val view = binding.root
        generateWordList(binding.wordRecyclerView)
        observeWordList(binding.wordListProgressBar)
        observeEmptyState()
        observeSearchBarStatus(binding.searchWrapper)
        setFabOnClickListener(binding.wordFab)
        setToolbarMenu(binding.wordListToolbar)
        searchWordViewSetup(binding.searchField, binding.clearSearch)
        searchBar = binding.searchWrapper
        searchField = binding.searchField

        return view
    }

    private fun onItemClick(word: WordItem) {
        val action = WordListFragmentDirections.fromWordListToWordDetails(wordViewModel.dictionaryId, word.wordData.wordId)
        findNavController().navigate(action)
    }

    private fun setToolbarMenu(toolbar: Toolbar) {
        toolbar.apply {
            title = args.dictionaryName
            setNavigationOnClickListener {
                if (searchBar?.isGone == true) {
                    findNavController().popBackStack()
                } else {
                    wordViewModel.setSearchBarStatus(false)
                }
            }
            wordViewModel.isListEmpty().observe(viewLifecycleOwner, Observer { isListEmpty ->
                inflateToolbarMenu(isListEmpty, toolbar)
            })
        }
    }

    private fun inflateToolbarMenu(isListEmpty: Boolean, toolbar: Toolbar) {
        toolbar.apply {
            if (!isListEmpty && toolbar.menu.size() == 0) {
                inflateMenu(R.menu.word_list_menu)
//                navigationIconsSet(menu.getItem(0).subMenu)
                setOnMenuItemClickListener { item: MenuItem? ->
//                    navigationIconsSet(menu.getItem(0).subMenu)
                    when (item?.itemId) {
                        R.id.start_quiz_from_word_list -> showStartQuizDialog(wordViewModel.dictionaryId)
                        R.id.export_dictionary -> shareDictionary()
                        R.id.sort_by_translation -> {
                            wordViewModel.setSortBy(wordViewModel.currentSortByData.copy(
                                    sortByOption = SortByOptions.SortByTranslation,
                                    translationDescending = !wordViewModel.currentSortByData.translationDescending)
                            )
                        }
                        R.id.sort_by_word -> {
                            wordViewModel.setSortBy(wordViewModel.currentSortByData.copy(
                                    sortByOption = SortByOptions.SortByWord,
                                    wordDescending = !wordViewModel.currentSortByData.wordDescending)
                            )
                        }
                        R.id.sort_by_date -> {
                            wordViewModel.setSortBy(wordViewModel.currentSortByData.copy(
                                    sortByOption = SortByOptions.SortByDate,
                                    dateDescending = !wordViewModel.currentSortByData.dateDescending)
                            )
                        }
                        R.id.search -> showSearchBar()
                    }
                    true
                }
            } else if (isListEmpty) {
                toolbar.menu.clear()
            }
        }
    }
    //TODO: Check why Menu needs to be added here. Why is it not enough to pass only the menuItem. Problem: menu.getItem[0].subMenu
    private fun navigationIconsSet(menu: Menu) {
        wordViewModel.currentSortByData.let {
            when (it.sortByOption) {
                SortByOptions.SortByDate -> setIcons(menu, R.id.sort_by_date, it.dateDescending)
                SortByOptions.SortByWord -> setIcons(menu, R.id.sort_by_word, it.wordDescending)
                SortByOptions.SortByTranslation -> setIcons(menu, R.id.sort_by_translation, it.translationDescending)
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

    private fun generateWordList(recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = wordAdapter
        }
    }

    private fun observeWordList(progressBar: ProgressBar) {
        progressBar.show(true)
        wordViewModel.getLiveWordList().observe(viewLifecycleOwner, Observer { data ->
            val wordList = data.first
            val isSearchOpen = data.second
            val items = ArrayList<Item<out GroupieViewHolder>>()
            items += wordList.map {
                WordItem(it) { selectedItem: WordItem ->
                    onItemClick(
                        selectedItem
                    )
                }
            }
            items += NumberOfWordsItem(
                String.format(
                    getString(R.string.number_of_words),
                    wordList.size
                )
            )
            wordAdapter.update(items)

            progressBar.show(false)
            showEmptyState(wordList.isEmpty() && !isSearchOpen)
            inflateToolbarMenu(wordList.isEmpty() && !isSearchOpen, binding.wordListToolbar)
        })
    }

    private fun observeEmptyState() {
        wordViewModel.isListEmpty().observe(viewLifecycleOwner, Observer {
            showEmptyState(it)
        })
    }

    private fun showEmptyState(show: Boolean) {
        binding.animationBook.show(show)
        binding.emptyStateMessageTitle.show(show)
        binding.emptyStateMessage.show(show)
        binding.wordColumnTitle.show(!show)
        binding.translationColumnTitle.show(!show)
        binding.wordRecyclerView.show(!show)
        if (show) wordViewModel.setSearchBarStatus(false)
    }

    private fun setFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            openCreateDialog()
            wordViewModel.setSearchBarStatus(false)
        }
    }

    private fun openCreateDialog() {
        createDialog = dialogFactory.buildWordCreateDialog(
                requireActivity(),
                createClick = { wordText, translationText ->
                    wordViewModel.insertWord(wordViewModel.createWordObject(wordText, translationText))
                    createDialog?.dismiss()
                }, addMoreClick = { wordText, translationText ->
            wordViewModel.insertWord(wordViewModel.createWordObject(wordText, translationText))
        })
        createDialog?.show()
    }

    private fun showStartQuizDialog(dictionaryId: Long) {
        startQuizDialog = dialogFactory.buildStartQuizDialog(
                dictionaryId,
                requireActivity(),
                args.dictionaryName) { selectedDirection: Int, id: Long, selectedQuizType: Int, customSize: Int? ->
            wordViewModel.addCustomQuizSize(customSize)
            startQuiz(selectedDirection, id, selectedQuizType)
            startQuizDialog?.dismiss()
        }
        startQuizDialog?.show()
        wordViewModel.setSearchBarStatus(false)
    }

    private fun startQuiz(selectedOption: Int, dictionaryId: Long, selectedQuiz: Int) {
        disposables += wordViewModel.startNew(dictionaryId, selectedQuiz.toQuizType())
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe {
                    val action = WordListFragmentDirections.fromWordListToQuiz(
                            dictionaryId,
                            selectedOption,
                            selectedQuiz
                    )
                    findNavController().navigate(action)
                }
    }

    private fun shareDictionary() {
        if (wordViewModel.getLiveWordList().value != null) {
            shareViewModel.shareDictionary(wordViewModel.getLiveWordList().value!!.first, requireContext())
        }
    }

    private fun searchWordViewSetup(searchField: EditText, clear: ImageView) {
        searchField.addTextChangedListener(SearchTextWatcher())
        searchField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) searchField.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            else searchField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0)
        }

        clear.setOnClickListener {
            searchField.text.clear()
        }
    }

    private fun showSearchBar() {
        wordViewModel.isSearchBarOpen().value?.let {
            wordViewModel.setSearchBarStatus(!it)
        }
    }

    private fun observeSearchBarStatus(searchWrapper: ConstraintLayout) {
        wordViewModel.isSearchBarOpen().observe(viewLifecycleOwner, Observer {
            searchWrapper.display(it)
            if (!it) binding.searchField.text?.clear()
        })
    }

    override fun onDestroy() {
        disposables.clear()
        createDialog?.dismiss()
        startQuizDialog?.dismiss()
        super.onDestroy()
    }

    private inner class SearchTextWatcher : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            wordViewModel.setSearchedTerm(p0.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}