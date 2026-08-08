package com.vocabulary.myvocabulary.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedMapData
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedWordRepository
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.GuessedWord
import com.vocabulary.myvocabulary.ui.quizzes.QuizDirectionType
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.ui.quizzes.toDirectionType
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.round
import java.text.Normalizer

class ResultViewModel(
    val dictionaryId: Long,
    val quizDirection: Int,
    private val wordRepository: WordRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val rxSchedulers: RxSchedulers,
    private val quizRepository: QuizRepository,
    private val guessedWordRepository: GuessedWordRepository,
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val guessedWordList: MutableStateFlow<List<Word>> = MutableStateFlow(emptyList())
    var isAllPassed = true

    private val numOfPassed: MutableStateFlow<Int> = MutableStateFlow(0)
    private val resultPercentage: MutableStateFlow<Int> = MutableStateFlow(0)

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun fetchGuessedList() {
        viewModelScope.launch {
            observeGuessedWordMap()
        }
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
                val calculatedPercentage = if (guessList.isNotEmpty()) {
                    round(((guessList.filter { it.lastResult }.size.toFloat() / guessList.size.toFloat()) * 100)).toInt()
                } else 0
                resultPercentage.value = calculatedPercentage
                saveQuizStats(dictionaryId, calculatedPercentage)
                saveLastPracticeOfDictionary(dictionaryId)
                guessedWordList.value = guessList
                quizRepository.updateQuizList(guessList)
                numOfPassed.value = guessList.filter { it.lastResult }.size
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
        val isCorrect = if (quizDirection.toDirectionType() == QuizDirectionType.AskWord) {
            it.translation.normalize().equals(entry.value.normalize(), ignoreCase = true)
        } else {
            it.word.normalize().equals(entry.value.normalize(), ignoreCase = true)
        }

        return if (isCorrect) {
            it.copy(
                lastResult = true,
                lastGuess = entry.value,
                beenAsked = it.beenAsked + 1,
                passed = it.passed + 1
            )
        } else {
            setAllPassedValue(false)
            it.copy(
                lastResult = false,
                lastGuess = entry.value,
                beenAsked = it.beenAsked + 1,
                failed = it.failed + 1
            )
        }
    }

    fun dispose() {
        disposables.clear()
    }

    fun getGuessedList() = guessedWordList


    fun resetGuessedWordCollections() {
        guessedWordRepository.resetGuessedWordMap()
        guessedWordList.value = emptyList()
        setAllPassedValue(true) // It's true until in evaluation it's turned false
    }

    private fun setAllPassedValue(lastResult: Boolean) {
        isAllPassed = lastResult
    }

    fun startNew(dictionaryId: Long, quizType: QuizTypes) {
        viewModelScope.launch {
            quizRepository.setQuizList(dictionaryId, quizType)
        }
    }

    fun latestGuess(lastGuess: GuessedWord) {
        guessedWordRepository.addToGuessedWordMap(lastGuess)
    }

    fun saveLastPracticeOfDictionary(dictionaryId: Long) {
        dictionaryRepository.onQuizFinished(dictionaryId)
    }

    fun saveQuizStats(id: Long, scorePercentage: Int) {
        dictionaryRepository.saveQuizStats(id, scorePercentage)
    }

    fun getNumOfPassed() = numOfPassed

    fun getResultPercentage() = resultPercentage
}

private fun String.normalize(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFC).trim()
}