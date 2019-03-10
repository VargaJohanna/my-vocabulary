package com.vocabulary.myvocabulary.room.wordData

import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.ui.words.toWordEntry
import io.reactivex.Observable
import io.reactivex.Single

class WordRepositoryImpl(
        private val wordDao: WordDao
) : WordRepository {

    override fun getObservableWordList(dictionaryId: Long): Observable<List<Word>> {
        return wordDao.getAllWordsInDictionary(dictionaryId)
                .map { list ->
                    list.map { it.toWord() }
                }
    }

    override fun getIsWordInDictionary(wordId: Long): Observable<Boolean> {
        return wordDao.getNumberOfWordById(wordId)
                .map { it != 0 }
    }

    override fun createWord(word: Word) = wordDao.insertWord(word.toWordEntry())

    override fun deleteWord(word: Word) = wordDao.deleteWord(word.toWordEntry())

    override fun updateWord(word: Word) = wordDao.updateWord(word.toWordEntry())

    override fun getWordById(wordId: Long): Single<Word> {
        return wordDao.getWordById(wordId).map {
            it.toWord()
        }
    }
}