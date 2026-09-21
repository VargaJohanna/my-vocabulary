package com.vocabulary.myvocabulary.repositories.user

import android.content.Context
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val currentUser: Flow<User?>
    val currentUserId: String?
    suspend fun loginWithGoogle(context: Context): Result<Unit>
    suspend fun logout(context: Context)
}