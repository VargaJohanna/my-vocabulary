package com.vocabulary.myvocabulary.repositories.sync

import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.words.Word
import kotlinx.coroutines.flow.Flow

interface CloudSyncRepository {
    suspend fun uploadDictionary(userId: String, dictionary: Dictionary, words: List<Word>): Result<Unit>
    suspend fun deleteDictionary(userId: String, dictionaryId: Long): Result<Unit>
    suspend fun downloadDictionaries(userId: String): Result<List<Pair<CloudDictionary, List<CloudWord>>>>
}
