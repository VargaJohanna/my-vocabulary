package com.vocabulary.myvocabulary.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.domain.ProcessQuizResultsUseCase
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedMapData
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedWordRepository
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
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
import kotlin.coroutines.cancellation.CancellationException

class ResultViewModel(
    val dictionaryId: Long,
    val quizDirection: Int,
    private val quizRepository: QuizRepository,
    private val guessedWordRepository: GuessedWordRepository,
    private val processQuizResultsUseCase: ProcessQuizResultsUseCase

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

                    if (map.isEmpty()) {
                        _resultUiState.value = ResultUiState.Error("There were no results to collect.")
                        return@collect
                    }

                    try {
                        val resultData = processQuizResultsUseCase(dictionaryId, map, quizDirection)

                        ensureActive()

                        quizRepository.updateQuizList(resultData.processedWords)

                        if (resultData.allPassed) {
                            _resultUiState.value = ResultUiState.Data(
                                resultList = resultData.processedWords,
                                percentage = resultData.percentage,
                                directionType = quizDirection.toDirectionType(),
                                allPassed = true,
                                numberOfPassed = resultData.processedWords.count { it.lastResult }
                            )
                        } else {
                            _resultUiState.value = ResultUiState.Data(
                                resultList = resultData.processedWords,
                                numberOfPassed = resultData.processedWords.count { it.lastResult },
                                percentage = resultData.percentage,
                                directionType = quizDirection.toDirectionType(),
                                allPassed = false
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
}

sealed interface ResultUiState {
    data object Loading : ResultUiState

    data class Data(
        val resultList: List<Word>,
        val numberOfPassed: Int,
        val percentage: Int,
        val directionType: QuizDirectionType,
        val allPassed: Boolean
    ) : ResultUiState

    data class Error(
        val message: String
    ) : ResultUiState
}