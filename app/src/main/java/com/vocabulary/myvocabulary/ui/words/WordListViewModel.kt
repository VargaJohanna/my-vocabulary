package com.vocabulary.myvocabulary.ui.words

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.search.SearchRepository
import com.vocabulary.myvocabulary.repositories.sortBy.SortByData
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class WordListViewModel(
        val dictionaryId: Long,
        private val sortByRepository: SortByRepository,
        private val wordRepository: WordRepository,
        private val sortedListRepository: SortedListRepository,
        private val rxSchedulers: RxSchedulers,
        private val quizRepository: QuizRepository,
        private val searchRepository: SearchRepository

) : ViewModel() {
    private val disposables = CompositeDisposable()
    private var listFromData: List<Word> = emptyList()
    private val liveWordList: MutableLiveData<Pair<List<Word>, Boolean>> = MutableLiveData()
    var currentSortByData: SortByData = SortByData()
    private val isListEmpty: MutableLiveData<Boolean> = MutableLiveData()
    private val isSearchBarOpenCurrent: MutableLiveData<Boolean> = MutableLiveData()
    var searchedTerm = ""

    init {
        observeList()
        observeSortByData()
        observeSearchBarStatus()
    }

    private fun observeSearchBarStatus() {
        disposables += searchRepository.showSearchBar()
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { t -> isSearchBarOpenCurrent.postValue(t) }
    }

    fun isSearchBarOpen(): LiveData<Boolean> = isSearchBarOpenCurrent

    fun setSearchBarStatus(isOpen: Boolean) {
        searchRepository.saveSearchBarStatus(isOpen)
    }

    private fun observeSortByData() {
        disposables += sortByRepository.sortByData()
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { t -> currentSortByData = t }
    }

    fun insertWord(word: Word) {
        disposables += Completable.fromCallable { wordRepository.createWord(word) }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe()
    }

    private fun observeList() {
        disposables += sortedListRepository.getSortedWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { t ->
                    isListEmpty.postValue(t.isEmpty() && !(isSearchBarOpenCurrent.value ?: false))
                    listFromData = t
                    liveWordList.postValue(Pair(
                            first = t.filter { it.translation.contains(searchedTerm, true) || it.word.contains(searchedTerm, true) },
                            second = isSearchBarOpenCurrent.value ?: false))
                }
    }

    fun getLiveWordList(): LiveData<Pair<List<Word>, Boolean>> = liveWordList

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun createWordObject(word: String, translation: String) = Word(containerDictionaryId = dictionaryId,
            word = word,
            translation = translation,
            created = Calendar.getInstance().time)

    fun updateWord(word: Word) {
        disposables += Completable.fromCallable {
            wordRepository.updateWord(word)
        }.subscribeOn(rxSchedulers.io())
                .subscribe()
    }

    fun deleteWord(word: Word) {
        disposables += Completable.fromCallable {
            wordRepository.deleteWord(word)
        }.subscribeOn(rxSchedulers.io())
                .subscribe()
    }

    fun startNew(dictionaryId: Long, quizType: QuizTypes): Completable {
        return quizRepository.resetQuizList(dictionaryId, quizType)
    }

    fun setSortBy(sortByData: SortByData) {
        sortByRepository.setSortBy(sortByData)
    }

    fun isListEmpty(): LiveData<Boolean> = isListEmpty

    fun searchList(find: String) {
        liveWordList.postValue(Pair(
                first = listFromData.filter { it.translation.contains(find, true) || it.word.contains(find, true) },
                second = isSearchBarOpenCurrent.value ?: false))
    }
}