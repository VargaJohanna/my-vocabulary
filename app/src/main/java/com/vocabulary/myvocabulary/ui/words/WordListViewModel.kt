package com.vocabulary.myvocabulary.ui.words

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.repositories.quiz.CustomQuizRepository
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.search.SearchRepository
import com.vocabulary.myvocabulary.repositories.sortBy.SortByData
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class WordListViewModel(
        val dictionaryId: Long,
        private val sortByRepository: SortByRepository,
        private val wordRepository: WordRepository,
        private val sortedListRepository: SortedListRepository,
        private val rxSchedulers: RxSchedulers,
        private val quizRepository: QuizRepository,
        private val searchRepository: SearchRepository,
        private val customQuizRepository: CustomQuizRepository

) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _wordList: MutableStateFlow<Pair<List<Word>, Boolean>> = MutableStateFlow(Pair(emptyList(), false))
    val wordList: StateFlow<Pair<List<Word>, Boolean>> = _wordList
    private val liveWordList: MutableLiveData<Pair<List<Word>, Boolean>> = MutableLiveData()
    var currentSortByData: SortByData = SortByData()
    private val isListEmpty: MutableLiveData<Boolean> = MutableLiveData()
    private val isSearchBarOpenCurrent: MutableLiveData<Boolean> = MutableLiveData()

    fun fetchWordList() {
        viewModelScope.launch {
            observeList()
            observeSearchBarStatus()
            observeSortByData()
        }
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
                .subscribe({},
                    {throwable ->
                        Log.e("DB_ERROR", "Could NOT insert word: ${throwable.message}")
                    })
    }

    private fun observeList() {
        disposables += Observable.combineLatest(
                sortedListRepository.getSortedWordList(dictionaryId),
                searchRepository.searchedTerm,
                BiFunction<List<Word>, String, List<Word>> { wordList, searchTerm -> searchList(wordList, searchTerm) })
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { t ->
                    isListEmpty.postValue(t.isEmpty() && !(isSearchBarOpenCurrent.value ?: false))
                    liveWordList.postValue(Pair(
                            first = t,
                            second = isSearchBarOpenCurrent.value ?: false))
                    _wordList.value = Pair(
                        first = t,
                        second = isSearchBarOpenCurrent.value ?: false)
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

    private fun searchList(wordList: List<Word>, find: String): List<Word> {
        return wordList.filter { it.translation.contains(find, true) || it.word.contains(find, true) }
    }

    fun setSearchedTerm(searchTerm: String) {
        searchRepository.setSearchedTerm(searchTerm)
    }

    fun addCustomQuizSize(size: Int?) {
        size?.let {
            customQuizRepository.quizSize = size
        }
    }
}