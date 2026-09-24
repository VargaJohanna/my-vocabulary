package com.vocabulary.myvocabulary.ui.home

import android.content.SharedPreferences
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val quoteRepository: QuoteRepository,
    private val shareDictionaryRepository: ShareDictionaryRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val preferences: SharedPreferences,
    private val dataStore: DataStore<Preferences>,
    private val wordRepository: WordRepository
) : ViewModel() {
    private val _quoteUiState = MutableStateFlow<QuoteUiState>(QuoteUiState.Loading)
    val quoteUiState: StateFlow<QuoteUiState> = _quoteUiState.asStateFlow()
    private val openedAppCounter: Int = preferences.getInt(COUNTER_KEY, 0)
    private val _isStatsLoading = MutableStateFlow(true)

    private val _lastPracticedDictionary = MutableStateFlow<Dictionary?>(null)
    val lastPracticedDictionary: StateFlow<Dictionary?> = _lastPracticedDictionary.asStateFlow()

    private val _mostPracticedDictionary = MutableStateFlow<Dictionary?>(null)
    val mostPracticedDictionary: StateFlow<Dictionary?> = _mostPracticedDictionary.asStateFlow()

    private val _leastPracticedDictionary = MutableStateFlow<Dictionary?>(null)
    val leastPracticedDictionary: StateFlow<Dictionary?> = _leastPracticedDictionary.asStateFlow()
    private val _memoriseList = MutableStateFlow<List<Word>>(emptyList())
    private val _numOfDictionaries = MutableStateFlow(0)

    val homeUiState: StateFlow<HomeUiState> = combine(
        _isStatsLoading,
        _quoteUiState,
        dictionaryRepository.isSyncing,
        dictionaryRepository.allDictionaries
    ) { isStatsLoading, quoteState, isSyncing, list ->
        val isLoading = (isStatsLoading && list.isEmpty()) || isSyncing || quoteState is QuoteUiState.Loading
        if (isLoading) {
            HomeUiState.Loading
        } else {
            HomeUiState.Success(
                lastPracticed = _lastPracticedDictionary.value,
                mostPracticed = _mostPracticedDictionary.value,
                leastPracticed = _leastPracticedDictionary.value,
                memoriseList = _memoriseList.value,
                numOfDictionaries = list.size
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    init {
        observeQuote()
        getDictionaryStats()
        observeMemoriseList()
    }

    private fun observeQuote() {
        viewModelScope.launch {
            _quoteUiState.value = QuoteUiState.Loading
            val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val lastDismissal = quoteDismissalFlow().first()

            quoteRepository.getQuote()
                .catch { e ->
                    _quoteUiState.value = QuoteUiState.Error(e.message ?: "Unknown error")
                    println("Quote Error: ${e.message}")
                }
                .collect {
                    _quoteUiState.value = QuoteUiState.Success(it, today != lastDismissal)
                    println("Quote: ${_quoteUiState.value}")
                }
        }
    }

    fun quoteDismissalFlow(): Flow<Int> = dataStore.data.map { pref ->
        pref[Keys.IS_QUOTE_DISMISSED_FOR_TODAY] ?: 0
    }

    fun dismissQuote() {
        val currentState = _quoteUiState.value
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        if (currentState is QuoteUiState.Success) {
            _quoteUiState.value = currentState.copy(isVisible = false)
            viewModelScope.launch {
                dataStore.updateData {
                    it.toMutablePreferences().also { pref ->
                        pref[Keys.IS_QUOTE_DISMISSED_FOR_TODAY] = currentDay
                    }
                }
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
            _isStatsLoading.value = true
            dictionaryRepository.allDictionaries
                .collect { list ->
                    _numOfDictionaries.value = list.size
                    _lastPracticedDictionary.value = list
                        .filter { it.dictionaryLastPracticed != null }
                        .maxByOrNull { it.dictionaryLastPracticed!!.time }

                    _mostPracticedDictionary.value = list
                        .maxByOrNull { it.dictionaryFinishedCount }

                    _leastPracticedDictionary.value = list
                        .minByOrNull { it.dictionaryFinishedCount }

                    _isStatsLoading.value = false
                }
        }
    }

    fun refreshMemoriseList() {
        observeMemoriseList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeMemoriseList() {
        leastPracticedDictionary
            .filterNotNull()
            .flatMapLatest { dict ->
                wordRepository.getObservableWordList(dict.dictionaryId)
            }
            .onEach { list ->
                if (list.isNotEmpty()) {
                    _memoriseList.value = processMemoriseList(list)
                }
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
    }
}

private object Keys {
    val IS_QUOTE_DISMISSED_FOR_TODAY = intPreferencesKey("IS_QUOTE_DISMISSED_FOR_TODAY")
}

sealed interface QuoteUiState {
    object Loading : QuoteUiState

    data class Success(
        val quote: QuoteData.Quote,
        val isVisible: Boolean = true
    ) : QuoteUiState

    data class Error(val message: String) : QuoteUiState
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val lastPracticed: Dictionary?,
        val mostPracticed: Dictionary?,
        val leastPracticed: Dictionary?,
        val memoriseList: List<Word>,
        val numOfDictionaries: Int
    ) : HomeUiState
    data class Error(
        val message: String
    ) : HomeUiState
}