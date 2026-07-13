package com.vocabulary.myvocabulary.ui.home

import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.quotes.QuoteData
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.quotes.QuoteRepository
import com.vocabulary.myvocabulary.repositories.share.ShareDictionaryRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.words.Word
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import java.util.Calendar

class HomeViewModel(
    private val quoteRepository: QuoteRepository,
    private val shareDictionaryRepository: ShareDictionaryRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val preferences: SharedPreferences,
    private val wordRepository: WordRepository
) : ViewModel() {
    private val _quoteUiState = MutableStateFlow<QuoteUiState>(QuoteUiState.Loading)
    val quoteUiState: StateFlow<QuoteUiState> = _quoteUiState.asStateFlow()
    private val openedAppCounter: Int = preferences.getInt(COUNTER_KEY, 0)
    private val _lastPracticedDictionary = MutableStateFlow<Dictionary?>(
        Dictionary(
            dictionaryId = 0,
            dictionaryName = "",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null,
            dictionaryLastResult = null,
            dictionaryFinishedCount = 0,
            dictionaryTotalScore = 0
        )
    )
    val lastPracticedDictionary: StateFlow<Dictionary?> = _lastPracticedDictionary.asStateFlow()
    private val _mostPracticedDictionary = MutableStateFlow<Dictionary?>(
        Dictionary(
            dictionaryId = 0,
            dictionaryName = "",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null,
            dictionaryLastResult = null,
            dictionaryFinishedCount = 0,
            dictionaryTotalScore = 0
        )
    )
    val mostPracticedDictionary: StateFlow<Dictionary?> = _mostPracticedDictionary.asStateFlow()

    private val _leastPracticedDictionary = MutableStateFlow<Dictionary?>(
        Dictionary(
            dictionaryId = 0,
            dictionaryName = "",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null,
            dictionaryLastResult = null,
            dictionaryFinishedCount = 0,
            dictionaryTotalScore = 0
        )
    )
    val leastPracticedDictionary: StateFlow<Dictionary?> = _leastPracticedDictionary.asStateFlow()
    private val _memoriseList = MutableStateFlow<List<Word>>(emptyList())
    val memoriseList: StateFlow<List<Word>> = _memoriseList.asStateFlow()
    private val _numOfDictionaries = MutableStateFlow(0)
    val numOfDictionaries: StateFlow<Int> = _numOfDictionaries.asStateFlow()
    private val _isLoadingWords = MutableStateFlow(false)
    val isLoadingWords: StateFlow<Boolean> = _isLoadingWords.asStateFlow()

    init {
        observeQuote()
        getDictionaryStats()
        observeMemoriseList()
    }

    private fun observeQuote() {
        viewModelScope.launch {
            _quoteUiState.value = QuoteUiState.Loading
            val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val lastDismissal = preferences.getInt(IS_QUOTE_DISMISSED_FOR_TODAY, -1)

            quoteRepository.getQuote()
                .catch { e ->
                    _quoteUiState.value = QuoteUiState.Error(e.message ?: "Unknown error")
                println("Quote Error: ${e.message}")}
                .collect {
                    _quoteUiState.value = QuoteUiState.Success(it, today != lastDismissal)
                println("Quote: ${_quoteUiState.value}")}
        }
    }

    fun dismissQuote() {
        val currentState = _quoteUiState.value
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        if(currentState is QuoteUiState.Success) {
            _quoteUiState.value = currentState.copy(isVisible = false)
            preferences.edit().apply {
                putInt(IS_QUOTE_DISMISSED_FOR_TODAY, currentDay)
                apply()
            }
        }
    }

    fun saveCsvData(csv: Uri) {
        shareDictionaryRepository.storeCsvData(csv)
    }

    fun setIsImport(isImport: Boolean) {
        shareDictionaryRepository.setIsImport(isImport)
    }

    fun openedAppCount() {
        preferences.edit().apply {
            putInt(COUNTER_KEY, openedAppCounter + 1)
            apply()
        }
    }

    private fun getDictionaryStats() {
        viewModelScope.launch {
            dictionaryRepository.allDictionaries
                .asFlow()
                .collect { list ->
                    _numOfDictionaries.value = list.size
                    _lastPracticedDictionary.value = list
                        .filter { it.dictionaryLastPracticed != null }
                        .maxByOrNull { it.dictionaryLastPracticed!!.time }

                    _mostPracticedDictionary.value = list
                        .maxByOrNull { it.dictionaryFinishedCount }

                    _leastPracticedDictionary.value = list
                        .minByOrNull { it.dictionaryFinishedCount }
                }
        }
    }

    fun refreshMemoriseList() {
        _isLoadingWords.value = true
        observeMemoriseList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeMemoriseList() {
        leastPracticedDictionary
            .filterNotNull()
            .flatMapLatest { dict ->
                wordRepository.getObservableWordList(dict.dictionaryId).asFlow()
            }
            .onEach { list ->
                if (list.isNotEmpty()) {
                    _memoriseList.value = processMemoriseList(list)
                }
                _isLoadingWords.value = false
            }
            .launchIn(viewModelScope)
    }
    private fun processMemoriseList(list: List<Word>): List<Word> {
        return list.sortedByDescending { it.beenAsked }
            .filter { !it.lastResult }
            .shuffled()
            .take(minOf(list.size, 3))
    }

    companion object {
        const val COUNTER_KEY = "COUNTER"

        const val IS_QUOTE_DISMISSED_FOR_TODAY = "IS_QUOTE_DISMISSED_FOR_TODAY"
    }
}

sealed interface QuoteUiState {
    object Loading: QuoteUiState

    data class Success(
        val quote: QuoteData.Quote,
        val isVisible: Boolean = true
    ): QuoteUiState
    data class Error(val message: String) : QuoteUiState
}