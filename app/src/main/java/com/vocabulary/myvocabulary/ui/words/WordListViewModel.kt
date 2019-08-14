package com.vocabulary.myvocabulary.ui.words

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.repositories.sortBy.SortByData
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
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
        private val quizRepository: QuizRepository

) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val liveWordList: MutableLiveData<List<Word>> = MutableLiveData()
    var currentSortByData: SortByData = SortByData()
    private val isListEmpty: MutableLiveData<Boolean> = MutableLiveData()

    init {
        observeList()
        observeSortByData()
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
                .subscribe {
                    t -> liveWordList.postValue(t)
                    isListEmpty.postValue(t.isEmpty())
                }
    }

    fun getLiveWordList(): LiveData<List<Word>> = liveWordList

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

}