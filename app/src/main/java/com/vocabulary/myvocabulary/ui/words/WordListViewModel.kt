package com.vocabulary.myvocabulary.ui.words

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _wordList: MutableStateFlow<Pair<List<Word>, Boolean>> = MutableStateFlow(Pair(emptyList(), false))
    val wordList: StateFlow<Pair<List<Word>, Boolean>> = _wordList
    var currentSortByData: SortByData = SortByData()
    private val _searchBarState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun fetchWordList() {
        viewModelScope.launch {
            observeList()
            observeSortByData()
        }
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
                    _wordList.value = Pair(
                        first = t,
                        second = _searchBarState.value
                    )
                }
    }

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

    fun startNew(dictionaryId: Long, quizType: QuizTypes) {
        viewModelScope.launch{
            quizRepository.setQuizList(dictionaryId, quizType)
        }
    }

    fun setSortBy(sortByData: SortByData) {
        sortByRepository.setSortBy(sortByData)
    }

    private fun searchList(wordList: List<Word>, find: String): List<Word> {
        return wordList.filter { it.translation.contains(find, true) || it.word.contains(find, true) }
    }

    fun setSearchedTerm(searchTerm: String) {
        searchRepository.setSearchedTerm(searchTerm)
    }
}