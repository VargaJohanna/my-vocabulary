package com.vocabulary.myvocabulary.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class WordDetailsViewModel(
        private val wordRepository: WordRepository
) : ViewModel() {

    private val _currentWord: MutableStateFlow<Word> = MutableStateFlow(Word(0, 0, "", "", 0, 0, 0, Calendar.getInstance().time))
    val currentWord: StateFlow<Word> = _currentWord

    fun fetchWordById(id: Long) {
        viewModelScope.launch {
            _currentWord.value = wordRepository.getWordById(id)
        }
    }
}