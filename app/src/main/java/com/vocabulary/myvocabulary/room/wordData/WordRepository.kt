package com.vocabulary.myvocabulary.room.wordData

import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable

interface WordRepository {
    fun createWord(word: Word)
    fun deleteWord(word: Word)
    fun updateWord(word: Word)
    fun getAllWords(): Observable<List<Word>>
    fun getWordById(wordId: Long): Observable<Word>
}