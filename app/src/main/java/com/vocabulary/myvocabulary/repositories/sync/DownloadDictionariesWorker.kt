package com.vocabulary.myvocabulary.repositories.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DownloadDictionariesWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val dictionaryRepository: DictionaryRepository by inject()
    private val firebaseAuth: FirebaseAuth by inject()

    override suspend fun doWork(): Result {
        val userId = firebaseAuth.currentUser?.uid ?: return Result.success()

        return try {
            val result = dictionaryRepository.syncFromCloud(userId)
            if (result.isSuccess) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
