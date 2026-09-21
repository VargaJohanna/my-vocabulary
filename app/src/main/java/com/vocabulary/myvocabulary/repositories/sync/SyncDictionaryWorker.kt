package com.vocabulary.myvocabulary.repositories.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncDictionaryWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val cloudSyncRepository: CloudSyncRepository by inject()
    private val dictionaryRepository: DictionaryRepository by inject()
    private val wordRepository: WordRepository by inject()
    private val firebaseAuth: FirebaseAuth by inject()

    override suspend fun doWork(): ListenableWorker.Result {
        val dictionaryId = inputData.getLong(KEY_DICTIONARY_ID, -1L)
        if (dictionaryId == -1L) return ListenableWorker.Result.failure()

        val userId = firebaseAuth.currentUser?.uid ?: return Result.success()

        return try {
            val dictionary = dictionaryRepository.getDictionaryById(dictionaryId)
            val words = wordRepository.getObservableWordList(dictionaryId).first()

            val result = cloudSyncRepository.uploadDictionary(userId, dictionary, words)
            
            if (result.isSuccess) ListenableWorker.Result.success() else ListenableWorker.Result.retry()
        } catch (e: Exception) {
            ListenableWorker.Result.retry()
        }
    }

    companion object {
        const val KEY_DICTIONARY_ID = "dictionary_id"
    }
}
