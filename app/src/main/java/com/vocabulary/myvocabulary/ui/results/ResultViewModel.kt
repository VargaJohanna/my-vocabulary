package com.vocabulary.myvocabulary.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.DispatcherProvider
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedMapData
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedWordRepository
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.quizzes.GuessedWord
import com.vocabulary.myvocabulary.ui.quizzes.QuizDirectionType
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.ui.quizzes.toDirectionType
import com.vocabulary.myvocabulary.ui.words.Word
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import kotlin.math.round
import java.text.Normalizer
import kotlin.coroutines.cancellation.CancellationException

class ResultViewModel(
    val dictionaryId: Long,
    val quizDirection: Int,
    private val wordRepository: WordRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val quizRepository: QuizRepository,
    private val guessedWordRepository: GuessedWordRepository,
    private val dispatchers: DispatcherProvider

) : ViewModel() {
    private val guessedWordList: MutableStateFlow<List<Word>> = MutableStateFlow(emptyList())

    private val _resultUiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val resultUiState: StateFlow<ResultUiState> = _resultUiState.asStateFlow()
    private var resultCollectionJob: Job? = null

    fun fetchResults() {
        if (resultCollectionJob?.isActive == true) return

        _resultUiState.value = ResultUiState.Loading

        resultCollectionJob = viewModelScope.launch {
            guessedWordRepository.guessedWordMap
                .collect { guessMapData ->
                    val map = when (guessMapData) {
                        is GuessedMapData.EMPTY -> emptyMap()
                        is GuessedMapData.GuessedData -> guessMapData.map
                    }

                    if (map.isEmpty()) return@collect

                    try {
                        val guessList = map.entries.map { entry ->
                            updateWordRepositorySuspend(entry.key, entry.value)
                        }

                        ensureActive()

                        val calculatedPercentage = if (guessList.isNotEmpty()) {
                            round(((guessList.filter { it.lastResult }.size.toFloat() / guessList.size.toFloat()) * 100)).toInt()
                        } else 0

                        saveQuizStats(dictionaryId, calculatedPercentage)
                        saveLastPracticeOfDictionary(dictionaryId)
                        quizRepository.updateQuizList(guessList)

                        if (guessList.all { it.lastResult }) {
                            _resultUiState.value = ResultUiState.Success(
                                resultList = guessList,
                                percentage = calculatedPercentage,
                                directionType = quizDirection.toDirectionType()
                            )
                        } else {
                            _resultUiState.value = ResultUiState.Failed(
                                resultList = guessList,
                                numberOfPassed = guessList.count { it.lastResult },
                                percentage = calculatedPercentage,
                                directionType = quizDirection.toDirectionType()
                            )
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        _resultUiState.value =
                            ResultUiState.Error("Failed to process results. Error: ${e.message}")
                    }
                }
        }
    }

    private suspend fun updateWordRepositorySuspend(wordId: Long, guess: String): Word =
        withContext(dispatchers.io) {
            val word = wordRepository.getWordById(wordId).await()
            val updatedWord = evaluate(word, guess)
            wordRepository.updateWord(updatedWord)
            updatedWord
        }

    private fun evaluate(word: Word, guess: String): Word {
        val isCorrect = if (quizDirection.toDirectionType() == QuizDirectionType.AskWord) {
            word.translation.normalize().equals(guess.normalize(), ignoreCase = true)
        } else {
            word.word.normalize().equals(guess.normalize(), ignoreCase = true)
        }

        return if (isCorrect) {
            word.copy(
                lastResult = true,
                lastGuess = guess,
                beenAsked = word.beenAsked + 1,
                passed = word.passed + 1
            )
        } else {
            word.copy(
                lastResult = false,
                lastGuess = guess,
                beenAsked = word.beenAsked + 1,
                failed = word.failed + 1
            )
        }
    }

    fun resetGuessedWordCollections() {
        resultCollectionJob?.cancel()
        resultCollectionJob = null
        guessedWordRepository.resetGuessedWordMap()
        guessedWordList.value = emptyList()
        _resultUiState.value = ResultUiState.Loading
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
    data object Loading : ResultUiState

    data class Failed(
        val resultList: List<Word>,
        val numberOfPassed: Int,
        val percentage: Int,
        val directionType: QuizDirectionType
    ) : ResultUiState

    data class Success(
        val resultList: List<Word>,
        val percentage: Int,
        val directionType: QuizDirectionType
    ) : ResultUiState

    data class Error(
        val message: String
    ) : ResultUiState
}