package com.vocabulary.myvocabulary.ui.results

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.QuizDirectionType
import com.vocabulary.myvocabulary.ui.quizzes.QuizRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.ui.quizzes.toQuizType
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class ResultViewModel(
        val dictionaryId: Long,
        val quizType: Int,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers,
        val quizRepository: QuizRepository
) : ViewModel() {
    private val disposables = CompositeDisposable()
    var guessedWordMap: MutableMap<Long, String> = mutableMapOf()
    private var _liveGuessedWordList: MutableList<Word> = mutableListOf()
    private val liveGuessedWordList: MutableLiveData<List<Word>> = MutableLiveData()
    var directionResult: QuizDirectionType = QuizDirectionType.AskWord
    var isAllPassed = true

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun getGuessResult() {
        disposables += Observable.fromIterable(guessedWordMap.entries)
                .flatMapSingle { entry ->
                    wordRepository.getWordById(entry.key)
                            .map {
                                if (!evaluateGuess(it)) setAllPassedValue(false)
                                when (evaluateGuess(it)) {
                                    true -> it.copy(lastResult = evaluateGuess(it), lastGuess = entry.value, beenAsked = it.beenAsked + 1, passed = it.passed + 1)
                                    false -> it.copy(lastResult = evaluateGuess(it), lastGuess = entry.value, beenAsked = it.beenAsked + 1, failed = it.failed + 1)
                                }
                            }
                            .doOnSuccess {
                                //                                quizRepository.currentQuizList.add(it)
                                wordRepository.updateWord(it)
                            }
                }.toList()
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { guessList ->
                    liveGuessedWordList.postValue(guessList)
                    quizRepository._quizList.onNext(guessList)
                }
    }

    fun getLiveGuessedList() = liveGuessedWordList

    private fun evaluateGuess(word: Word): Boolean {
        return if (directionResult == QuizDirectionType.AskWord) {
            word.translation == guessedWordMap[word.wordId]
        } else {
            word.word == guessedWordMap[word.wordId]
        }
    }

    fun resetGuessedWordCollections() {
        guessedWordMap = mutableMapOf()
        _liveGuessedWordList = mutableListOf()
        liveGuessedWordList.postValue(_liveGuessedWordList)
        isAllPassed = true
    }

    fun setDirection(direction: QuizDirectionType) {
        this.directionResult = direction
    }

    private fun setAllPassedValue(lastResult: Boolean) {
        isAllPassed = lastResult
    }

    fun resetQuizList() {
        when (quizType.toQuizType()) {
            QuizTypes.FullQuiz -> quizRepository.resetFullQuizList(dictionaryId)
            QuizTypes.QuickQuiz -> quizRepository.resetQuickQuizList(dictionaryId)
//            QuizTypes.WeakestTenQuiz -> observeWeakestList(dictionaryId, failedOnly)
        }
    }

}