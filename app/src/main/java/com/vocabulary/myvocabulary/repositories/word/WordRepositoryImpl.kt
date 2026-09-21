package com.vocabulary.myvocabulary.repositories.word

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.vocabulary.myvocabulary.repositories.sync.SyncDictionaryWorker
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.ui.words.toWordEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WordRepositoryImpl(
    private val wordDao: WordDao,
    private val workManager: WorkManager
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

    override fun createWord(word: Word) {
        wordDao.insertWord(word.toWordEntry())
        triggerSync(word.containerDictionaryId)
    }

    override fun deleteWord(word: Word) {
        wordDao.deleteWord(word.toWordEntry())
        triggerSync(word.containerDictionaryId)
    }

    override fun updateWord(word: Word) {
        wordDao.updateWord(word.toWordEntry())
        triggerSync(word.containerDictionaryId)
    }

    private fun triggerSync(dictionaryId: Long) {
        val syncRequest = OneTimeWorkRequestBuilder<SyncDictionaryWorker>()
            .setInputData(
                Data.Builder()
                    .putLong(SyncDictionaryWorker.KEY_DICTIONARY_ID, dictionaryId)
                    .build()
            )
            .build()
        workManager.enqueue(syncRequest)
    }

    override suspend fun getWordById(wordId: Long) = wordDao.getWordById(wordId).toWord()
}