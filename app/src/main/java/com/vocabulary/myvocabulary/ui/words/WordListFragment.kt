package com.vocabulary.myvocabulary.ui.words

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
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
import com.vocabulary.myvocabulary.repositories.sortBy.SortByOptions
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.toQuizType
import com.vocabulary.myvocabulary.utils.DialogFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_word_list.*
import kotlinx.android.synthetic.main.fragment_word_list.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.viewModel
import org.koin.core.parameter.parametersOf

class WordListFragment : Fragment(), WordAdapter.WordItemClickListener {
    private val args by navArgs<WordListFragmentArgs>()
    private val wordViewModel: WordListViewModel by viewModel {
        parametersOf(args.dictionaryId)
    }
    private val dialogFactory: DialogFactory by inject()
    private val rxSchedulers: RxSchedulers by inject()
    private var createDialog: AlertDialog? = null
    private var renameDialog: AlertDialog? = null
    private var startQuizDialog: AlertDialog? = null
    private var popUp: PopupMenu? = null
    private val disposables = CompositeDisposable()

    override fun onItemClick(word: Word) {
        val action = WordListFragmentDirections.fromWordListToWordDetails(wordViewModel.dictionaryId, word.wordId)
        findNavController().navigate(action)
    }

    override fun onOptionsClick(word: Word, view: View) {
        createPopUpMenu(word, view)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val wordAdapter = WordAdapter(emptyList(), this)
        return inflater.inflate(R.layout.fragment_word_list, container, false).apply {
            generateWordList(wordAdapter, word_recycler_view)
            observeWordList(wordAdapter, word_list_progress_bar)
            observeEmptyState()
            setFabOnClickListener(word_fab)
            setToolbarMenu(word_list_toolbar)
        }
    }

    private fun setToolbarMenu(toolbar: Toolbar) {
        toolbar.apply {
            inflateMenu(R.menu.word_list_menu)
            navigationIconsSet(menu.getItem(0).subMenu)
            setOnMenuItemClickListener { item: MenuItem? ->
                navigationIconsSet(menu.getItem(0).subMenu)
                when (item?.itemId) {
                    R.id.start_quiz_from_word_list -> showStartQuizDialog(wordViewModel.dictionaryId)
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
                }
                true
            }
            title = args.dictionaryName
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }

        }
    }

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

    private fun generateWordList(wordAdapter: WordAdapter, recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = wordAdapter
        }
    }

    private fun observeWordList(wordAdapter: WordAdapter, progressBar: ProgressBar) {
        progressBar.show(true)
        wordViewModel.getLiveWordList().observe(requireActivity(), Observer {
            wordAdapter.updateList(it)
            progressBar.show(false)
            wordViewModel.isListEmpty.postValue(it.isEmpty())
        })
    }

    private fun observeEmptyState() {
        wordViewModel.isListEmpty.observe(requireActivity(), Observer {
            if(animation_book != null) {
                showEmptyState(it)
            }
        })
    }

    private fun showEmptyState(show: Boolean) {
        animation_book.show(show)
        empty_state_message_title.show(show)
        empty_state_message.show(show)
        word_column_title.show(!show)
        translation_column_title.show(!show)
    }

    private fun setFabOnClickListener(fab: FloatingActionButton) {
        fab.setOnClickListener {
            openCreateDialog()
        }
    }

    private fun createPopUpMenu(word: Word, view: View) {
        popUp = PopupMenu(requireActivity(), view).apply {
            inflate(R.menu.word_options_menu)

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_word_edit -> showEditDialog(word)
                    R.id.menu_word_delete -> showDeleteWordDialog(word)
                }
                true
            }
            show()
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

    private fun showDeleteWordDialog(word: Word) {
        dialogFactory.buildDeleteWordDialog(
                requireActivity(),
                getString(R.string.dialog_delete_word_title),
                "${getString(R.string.verify_deletion)}\n\"${word.translation} - ${word.word}\" ?") {
            wordViewModel.deleteWord(word)
        }.show()
    }

    private fun showEditDialog(word: Word) {
        renameDialog = dialogFactory.buildWordEditDialog(
                requireActivity(),
                word) { wordInput, translationInput ->
            wordViewModel.updateWord(word.copy(word = wordInput, translation = translationInput))
            renameDialog?.dismiss()
        }
        renameDialog?.show()
    }

    private fun showStartQuizDialog(dictionaryId: Long) {
        startQuizDialog = dialogFactory.buildStartQuizDialog(
                dictionaryId,
                requireActivity(),
                args.dictionaryName) { selectedDirection: Int, id: Long, selectedQuizType: Int ->
            startQuiz(selectedDirection, id, selectedQuizType)
            startQuizDialog?.dismiss()
        }
        startQuizDialog?.show()
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

    override fun onStop() {
        disposables.clear()
        createDialog?.dismiss()
        renameDialog?.dismiss()
        startQuizDialog?.dismiss()
        popUp?.dismiss()
        super.onStop()
    }
}