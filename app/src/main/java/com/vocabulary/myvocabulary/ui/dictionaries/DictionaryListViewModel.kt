package com.vocabulary.myvocabulary.ui.dictionaries

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.DispatcherProvider
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryData
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.utils.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

class DictionaryListViewModel(
        private val dictionaryRepository: DictionaryRepository,
        private val sortByRepository: SortDictionaryRepository,
        private val sortedListRepository: SortedListRepository,
        private val dispatchers: DispatcherProvider,
        private val shareDictViewModel: ShareDictionaryViewModel,
        private val contentResolver: ContentResolver
) : ViewModel() {
    private val _dictionaries: MutableStateFlow<List<Dictionary>> = MutableStateFlow(emptyList())
    val dictionaries: StateFlow<List<Dictionary>> = _dictionaries.asStateFlow()

    var currentSortByData: SortDictionaryData = SortDictionaryData()
    private var _libraryUiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val libraryUiState: StateFlow<LibraryUiState> = _libraryUiState.asStateFlow()
    private var _createDictionaryEvent = MutableStateFlow<Event<DictionaryDetails?>?>(null)
    val createDictionaryEvent: StateFlow<Event<DictionaryDetails?>?> = _createDictionaryEvent.asStateFlow()
    private val _activeDialog = MutableStateFlow<DictionaryDialog?>(null)
    val activeDialog = _activeDialog.asStateFlow()

    private val _events = Channel<LibraryEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeSortByData()
        observeList()
        observeImportedDictionary()
    }

    fun insertDictionary(dictionary: Dictionary) {
        viewModelScope.launch {
            _libraryUiState.value = LibraryUiState.Loading
            try {
                val id = withContext(dispatchers.io) {
                    dictionaryRepository.createDictionary(dictionary)
                }
                val details = DictionaryDetails(id, dictionary.dictionaryName)
                _createDictionaryEvent.value = Event(details)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _libraryUiState.value = LibraryUiState.Error("Failed to insert dictionary. Error: ${e.message}")
            }
        }
    }

    private fun observeList() {
        viewModelScope.launch {
            sortedListRepository.getSortedDictionaryList().asFlow()
                .catch { e ->
                    if (e is CancellationException) throw e
                    _libraryUiState.value = LibraryUiState.Error("Failed to observe list. Error: ${e.message}")
                }
                .collect {
                    if (it.isEmpty()) {
                        _libraryUiState.value = LibraryUiState.Empty
                    } else {
                        _libraryUiState.value = LibraryUiState.LibraryData(it)
                    }
                }
        }
    }

    private fun observeImportedDictionary() {
        viewModelScope.launch {
            shareDictViewModel.importedDictionaryDetailsFlow.collect { event ->
                event.getContentIfNotHandled()?.let { details ->
                    shareDictViewModel.parseDataAndCreateWordsCompose(
                        dictionaryId = details.dictionaryId,
                        contentResolver = contentResolver
                    )
                    _events.send(LibraryEvent.NavigateToWordList(details.dictionaryId, details.dictionaryName))
                    Log.d("Import", "Created ${details.dictionaryName}, starting CSV parse...")
                }
            }
        }
    }

    fun clearNewDictionary() {
        _createDictionaryEvent.value = Event(null)
    }

    fun createDictionaryObject(dictionaryName: String): Dictionary = Dictionary(dictionaryName = dictionaryName,
            dictionaryCreated = Calendar.getInstance().time, dictionaryLastPracticed = null, dictionaryLastResult = null, dictionaryFinishedCount = 0, dictionaryTotalScore = 0)

    fun renameDictionary(dictionary: Dictionary) {
        viewModelScope.launch(dispatchers.io) {
            dictionaryRepository.updateDictionary(dictionary)
        }
    }

    fun deleteDictionary(dictionary: Dictionary) {
        viewModelScope.launch(dispatchers.io) {
            dictionaryRepository.deleteDictionary(dictionary)
        }
    }

    fun saveCsvData(uri: Uri) {
        shareDictViewModel.saveCsvData(uri)
    }

    fun setIsImport(isImport: Boolean) {
        shareDictViewModel.setIsImport(isImport)
    }

    fun createImportedDictionary(dictionary: Dictionary) {
        shareDictViewModel.createDictionary(dictionary)
    }

    private fun observeSortByData() {
        viewModelScope.launch {
            sortByRepository.sortByData().asFlow().collect {
                currentSortByData = it
            }
        }
    }

    fun setSortBy(sortByData: SortDictionaryData) {
        sortByRepository.setSortBy(sortByData)
    }

    fun setActiveDialogState(dictionaryDialog: DictionaryDialog) {
        _activeDialog.value = dictionaryDialog
    }

    fun clearActiveDialogState() {
        _activeDialog.value = null
    }
}

sealed interface LibraryEvent {
    data class NavigateToWordList(val dictionaryId: Long, val dictionaryName: String) : LibraryEvent
}

sealed interface LibraryUiState {
    object Loading : LibraryUiState
    object Empty : LibraryUiState

    data class Error (
        val message: String
    ) : LibraryUiState

    data class LibraryData(
        val dictionaryList: List<Dictionary>
    ) : LibraryUiState
}

sealed class DictionaryDialog {
    data class Edit(val dictionary: Dictionary) : DictionaryDialog()
    data class Delete(val dictionary: Dictionary) : DictionaryDialog()
    object Create : DictionaryDialog()
    object Import : DictionaryDialog()
}
