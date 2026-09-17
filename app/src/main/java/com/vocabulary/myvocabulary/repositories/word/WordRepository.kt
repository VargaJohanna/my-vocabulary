package com.vocabulary.myvocabulary.repositories.word

import com.vocabulary.myvocabulary.ui.words.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun createWord(word: Word)
    fun deleteWord(word: Word)
    fun updateWord(word: Word)
    suspend fun getWordById(wordId: Long): Word
    fun getObservableWordList(dictionaryId: Long): Flow<List<Word>>
    fun getIsWordInDictionary(wordId: Long): Flow<Boolean>
}