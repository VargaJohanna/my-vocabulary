package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.DispatcherProvider
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryData
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.utils.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.withContext
import java.util.*

class DictionaryListViewModel(
        private val dictionaryRepository: DictionaryRepository,
        private val sortByRepository: SortDictionaryRepository,
        private val sortedListRepository: SortedListRepository,
        private val dispatchers: DispatcherProvider
) : ViewModel() {
    private val _dictionaries: MutableStateFlow<List<Dictionary>> = MutableStateFlow(emptyList())
    val dictionaries: StateFlow<List<Dictionary>> = _dictionaries.asStateFlow()
    private val _newDictionary = MutableStateFlow<Event<DictionaryDetails?>>(Event(null)
    )
    val newDictionary: StateFlow<Event<DictionaryDetails?>> = _newDictionary.asStateFlow()
    var currentSortByData: SortDictionaryData = SortDictionaryData()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    init {
        observeSortByData()
        observeList()
    }
    fun fetchDictionaries() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                observeList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun insertDictionary(dictionary: Dictionary) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val id = withContext(dispatchers.io) {
                    dictionaryRepository.createDictionary(dictionary)
                }
                val details = DictionaryDetails(id, dictionary.dictionaryName)
                _newDictionary.value = Event(details)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeList() {
        viewModelScope.launch {
            sortedListRepository.getSortedDictionaryList().asFlow().collect {
                _dictionaries.value = it
            }
        }
    }

    fun clearNewDictionary() {
        _newDictionary.value = Event(null)
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
}
