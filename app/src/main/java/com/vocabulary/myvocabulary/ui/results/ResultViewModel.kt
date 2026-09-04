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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _resultUiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val resultUiState: StateFlow<ResultUiState> = _resultUiState.asStateFlow()

    init {
        viewModelScope.launch {
            initialSetup()
        }
    }

    private fun initialSetup() {
        _resultUiState.value = ResultUiState.Loading
         try {

         }

    }

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
                saveQuizStats(dictionaryId, calculatedPercentage)
                saveLastPracticeOfDictionary(dictionaryId)
                quizRepository.updateQuizList(guessList)
                if(guessList.filter { it.lastResult }.size == guessList.size) {
                    _resultUiState.value = ResultUiState.Success(
                        resultList = guessList,
                        percentage = calculatedPercentage,
                        directionType = quizDirection.toDirectionType()
                    )
                } else {
                    _resultUiState.value = ResultUiState.Failed(
                        resultList = guessList,
                        numberOfPassed = guessList.filter { it.lastResult }.size,
                        percentage = calculatedPercentage,
                        directionType = quizDirection.toDirectionType()
                    )
                }
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
}

private fun String.normalize(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFC).trim()
}

sealed interface ResultUiState {
    data object Loading: ResultUiState

    data class Failed(
        val resultList: List<Word>,
        val numberOfPassed: Int,
        val percentage: Int,
        val directionType: QuizDirectionType
    ): ResultUiState

    data class Success(
        val resultList: List<Word>,
        val percentage: Int,
        val directionType: QuizDirectionType
    ): ResultUiState

    data class Error(
        val message: String
    ): ResultUiState
}