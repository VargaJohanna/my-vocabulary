package com.vocabulary.myvocabulary.ui.words

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.room.wordData.SortByRepository
import com.vocabulary.myvocabulary.room.wordData.SortedListRepository
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.QuizRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.utils.SortByOptions
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import java.util.*

class WordListViewModel(
        val dictionaryId: Long,
        context: Context,
        private val wordRepository: WordRepository,
        private val sortedListRepository: SortedListRepository,
        private val rxSchedulers: RxSchedulers,
        private val quizRepository: QuizRepository

) : ViewModel(), KoinComponent {
    private val sortByRepository: SortByRepository by inject {
        parametersOf(context)
    }
    private val disposables = CompositeDisposable()
    private val liveWordList: MutableLiveData<List<Word>> = MutableLiveData()

    init {
        observeList()
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
                .subscribe { t -> liveWordList.postValue(t) }
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

    fun setSortBy(sort: SortByOptions, descending: Boolean) {
        sortByRepository.setSortBy(sort, descending)
    }

    fun isWordDescending() = sortByRepository.wordDescending
    fun isTranslationDescending() = sortByRepository.translationDescending
    fun isDateDescending() = sortByRepository.dateDescending

    fun setWordDescending(descending: Boolean) {
        sortByRepository.wordDescending = descending
    }

    fun setTranslationDescending(descending: Boolean) {
        sortByRepository.translationDescending = descending
    }

    fun setDateDescending(descending: Boolean) {
        sortByRepository.dateDescending = descending
    }

    fun defaultSortByOption() = sortByRepository.defaultSortBy

}