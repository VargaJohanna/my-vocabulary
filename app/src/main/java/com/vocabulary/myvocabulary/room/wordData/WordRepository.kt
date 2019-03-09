package com.vocabulary.myvocabulary.room.wordData

import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Single

interface WordRepository {
    fun createWord(word: Word)
    fun deleteWord(word: Word)
    fun updateWord(word: Word)
    fun getAllWords(dictionaryId: Long): Single<List<Word>>
    fun getWordById(wordId: Long): Single<Word>
    fun isWordIdInDictionary(wordId: Long): Single<Boolean>
}