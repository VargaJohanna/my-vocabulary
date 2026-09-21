package com.vocabulary.myvocabulary.repositories.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DeleteDictionaryWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val cloudSyncRepository: CloudSyncRepository by inject()
    private val firebaseAuth: FirebaseAuth by inject()

    override suspend fun doWork(): Result {
        val dictionaryId = inputData.getLong(KEY_DICTIONARY_ID, -1L)
        if (dictionaryId == -1L) return Result.failure()

        val userId = firebaseAuth.currentUser?.uid ?: return Result.success()

        return try {
            val result = cloudSyncRepository.deleteDictionary(userId, dictionaryId)
            if (result.isSuccess) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_DICTIONARY_ID = "dictionary_id"
    }
}
