package com.vocabulary.myvocabulary.repositories.word

import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import io.reactivex.Single

interface WordRepository {
    fun createWord(word: Word)
    fun deleteWord(word: Word)
    fun updateWord(word: Word)
    fun getWordById(wordId: Long): Single<Word>
    fun getObservableWordList(dictionaryId: Long): Observable<List<Word>>
    fun getIsWordInDictionary(wordId: Long): Observable<Boolean>
}