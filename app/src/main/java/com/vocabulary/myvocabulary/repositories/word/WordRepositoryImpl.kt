package com.vocabulary.myvocabulary.repositories.word

import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.ui.words.toWordEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WordRepositoryImpl(
        private val wordDao: WordDao
) : WordRepository {

    override fun getObservableWordList(dictionaryId: Long): Flow<List<Word>> {
        return wordDao.getAllWordsInDictionary(dictionaryId)
                .map { list ->
                    list.map { it.toWord() }
                }
    }

    override fun getIsWordInDictionary(wordId: Long): Flow<Boolean> {
        return wordDao.getNumberOfWordById(wordId)
                .map { it != 0 }
    }

    override fun createWord(word: Word) = wordDao.insertWord(word.toWordEntry())

    override fun deleteWord(word: Word) = wordDao.deleteWord(word.toWordEntry())

    override fun updateWord(word: Word) = wordDao.updateWord(word.toWordEntry())

    override suspend fun getWordById(wordId: Long) = wordDao.getWordById(wordId).toWord()
}