package com.vocabulary.myvocabulary.repositories.sortedList

import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.words.Word
import kotlinx.coroutines.flow.Flow

interface SortedListRepository {
    fun getSortedWordList(dictionaryId: Long): Flow<List<Word>>
    fun getSortedDictionaryList(): Flow<List<Dictionary>>
}