package com.vocabulary.myvocabulary.room.wordData

import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import io.reactivex.Single

interface WordRepository {
    val allWords: Observable<List<Word>>
    val isWordInDictionary: Observable<Boolean>
    fun createWord(word: Word)
    fun deleteWord(word: Word)
    fun updateWord(word: Word)
    fun getWordById(wordId: Long): Single<Word>
    fun observeAllList(dictionaryId: Long)
    fun observeIfWordIsInDictionary(wordId: Long)
}