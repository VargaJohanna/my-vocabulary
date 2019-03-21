package com.vocabulary.myvocabulary.ui.results

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.QuizDirectionType
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class ResultViewModel(
        val dictionaryId: Long,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {
    private val disposables = CompositeDisposable()
    var guessedWordMap: MutableMap<Long, String> = emptyMap<Long, String>().toMutableMap()
    private var _liveGuessedWordList: MutableList<Word> = emptyList<Word>().toMutableList()
    private val liveGuessedWordList: MutableLiveData<List<Word>> = MutableLiveData()
    var directionResult: QuizDirectionType = QuizDirectionType.AskWord

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }

    fun getGuessResult(){
        disposables += Observable.fromIterable(guessedWordMap.entries)
                .flatMapSingle { entry ->
                    wordRepository.getWordById(entry.key)
                            .map {
                                when(evaluateGuess(it)) {
                                    true -> it.copy(lastResult = evaluateGuess(it), lastGuess = entry.value, beenAsked = it.beenAsked + 1,  passed = it.passed + 1)
                                    false -> it.copy(lastResult = evaluateGuess(it), lastGuess = entry.value, beenAsked = it.beenAsked + 1, failed = it.failed + 1)
                                }
                            }
                            .doOnSuccess {
                                wordRepository.updateWord(it)
                            }
                }.toList()
                .map {
                    it
                }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { t -> liveGuessedWordList.postValue(t) }
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
        guessedWordMap = emptyMap<Long, String>().toMutableMap()
        _liveGuessedWordList = emptyList<Word>().toMutableList()
        liveGuessedWordList.postValue(_liveGuessedWordList)
    }

    fun setDirection(direction: QuizDirectionType) {
        this.directionResult = direction
    }

}