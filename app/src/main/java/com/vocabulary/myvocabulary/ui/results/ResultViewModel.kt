package com.vocabulary.myvocabulary.ui.results

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedMapData
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedWordRepository
import com.vocabulary.myvocabulary.repositories.quiz.CustomQuizRepository
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.home.HomeViewModel.Companion.COUNTER_KEY
import com.vocabulary.myvocabulary.ui.quizzes.GuessedWord
import com.vocabulary.myvocabulary.ui.quizzes.QuizDirectionType
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.Locale
import java.util.Locale.getDefault

class ResultViewModel(
        val dictionaryId: Long,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers,
        private val quizRepository: QuizRepository,
        private val guessedWordRepository: GuessedWordRepository,
        preferences: SharedPreferences

) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val liveGuessedWordList: MutableLiveData<List<Word>> = MutableLiveData()
    var directionResult: QuizDirectionType = QuizDirectionType.AskWord
    var isAllPassed = true
    val openedAppCounter: Int = preferences.getInt(COUNTER_KEY, 0)

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun observeGuessedWordMap() {
        disposables += guessedWordRepository.guessedWordMap
                .map {
                    when (it) {
                        is GuessedMapData.EMPTY -> mutableMapOf()
                        is GuessedMapData.GuessedData -> {
                            it.map.toMutableMap()
                        }
                    }
                }.flatMapSingle {
                    Observable.fromIterable(it.entries)
                            .subscribeOn(rxSchedulers.io())
                            .flatMapSingle { entry -> updateWordRepository(entry) }
                            .toList()
                }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { guessList ->
                    liveGuessedWordList.postValue(guessList)
                    quizRepository.updateQuizList(guessList)
                }
    }

    private fun updateWordRepository(entry: MutableMap.MutableEntry<Long, String>): Single<Word> {
        return wordRepository.getWordById(entry.key)
                .map {
                    evaluate(it, entry)
                }
                .doOnSuccess {
                    wordRepository.updateWord(it)
                }
    }

    private fun evaluate(it: Word, entry: MutableMap.MutableEntry<Long, String>): Word {
        return if (directionResult == QuizDirectionType.AskWord) {
            if (it.translation.lowercase(getDefault()) == entry.value.lowercase(getDefault())) {
                it.copy(lastResult = true, lastGuess = entry.value, beenAsked = it.beenAsked + 1, passed = it.passed + 1)
            } else {
                setAllPassedValue(false)
                it.copy(lastResult = false, lastGuess = entry.value, beenAsked = it.beenAsked + 1, failed = it.failed + 1)
            }
        } else {
            if (it.word.lowercase(getDefault()) == entry.value.lowercase(getDefault())) {
                it.copy(lastResult = true, lastGuess = entry.value, beenAsked = it.beenAsked + 1, passed = it.passed + 1)
            } else {
                setAllPassedValue(false)
                it.copy(lastResult = false, lastGuess = entry.value, beenAsked = it.beenAsked + 1, failed = it.failed + 1)
            }
        }
    }

    fun dispose() {
        disposables.clear()
    }

    fun getLiveGuessedList() = liveGuessedWordList


    fun resetGuessedWordCollections() {
        guessedWordRepository.resetGuessedWordMap()
        liveGuessedWordList.postValue(mutableListOf())
        setAllPassedValue(true)
    }

    fun setDirection(direction: QuizDirectionType) {
        this.directionResult = direction
    }

    private fun setAllPassedValue(lastResult: Boolean) {
        isAllPassed = lastResult
    }

    fun startNew(dictionaryId: Long, quizType: QuizTypes): Completable {
        return quizRepository.resetQuizList(dictionaryId, quizType)
    }

    fun latestGuess(lastGuess: GuessedWord) {
        guessedWordRepository.addToGuessedWordMap(lastGuess)
    }
}