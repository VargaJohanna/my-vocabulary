package com.vocabulary.myvocabulary.ui.user

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.user.User
import com.vocabulary.myvocabulary.repositories.user.UserRepository
import com.vocabulary.myvocabulary.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class LoginViewModel(
    private val userRepository: UserRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val externalScope: CoroutineScope
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _showMobileDataWarning = MutableStateFlow(false)
    val showMobileDataWarning: StateFlow<Boolean> = _showMobileDataWarning.asStateFlow()

    val authState: StateFlow<AuthState> = userRepository.currentUser
        .map { user ->
            if (user != null) AuthState.Authenticated(user) else AuthState.Unauthenticated
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Loading
        )

    val currentUser: StateFlow<User?> = userRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun onLoginClick(context: Context) {
        Log.d("Sync", "onLoginClick triggered")
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = userRepository.loginWithGoogle(context)
            Log.d("Sync", "loginWithGoogle returned success: ${result.isSuccess}")
            if (result.isSuccess) {
                val isMobile = NetworkUtils.isMobileDataActive(context)
                
                var uid: String? = null
                repeat(3) {
                    uid = userRepository.currentUserId
                    if (uid != null) return@repeat
                    delay(100.milliseconds)
                }

                if (uid == null) {
                    Log.e("Sync", "Login success but UID is STILL null after retries")
                    _uiState.value = LoginUiState.Error("User ID not found")
                    return@launch
                }
                
                Log.d("Sync", "Login success, UID: $uid")

                if (isMobile) {
                    _showMobileDataWarning.value = true
                } else {
                    // Sync immediately on Wi-Fi - use externalScope so it survives VM clearance
                    externalScope.launch {
                        val syncResult = dictionaryRepository.syncFromCloud(uid, requireWifi = false)
                        if (syncResult.isSuccess) {
                            dictionaryRepository.syncAllToCloud(requireWifi = false)
                        } else {
                            Log.e("Sync", "Background download failed: ${syncResult.exceptionOrNull()?.message}")
                        }
                    }
                    _uiState.value = LoginUiState.Success
                }
            } else {
                _uiState.value = LoginUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun onConfirmSync(proceed: Boolean) {
        viewModelScope.launch {
            _showMobileDataWarning.value = false
            _uiState.value = LoginUiState.Loading
            val uid = userRepository.currentUserId
            if (uid == null) {
                _uiState.value = LoginUiState.Error("User ID not found")
                return@launch
            }

            externalScope.launch {
                val syncResult = dictionaryRepository.syncFromCloud(uid, requireWifi = !proceed)
                if (syncResult.isSuccess) {
                    dictionaryRepository.syncAllToCloud(requireWifi = !proceed)
                } else {
                    Log.e("Sync", "Background sync failed: ${syncResult.exceptionOrNull()?.message}")
                }
            }
            _uiState.value = LoginUiState.Success
        }
    }

    fun onLogoutClick(context: Context) {
        viewModelScope.launch {
            dictionaryRepository.clearLocalData()
            userRepository.logout(context)
        }
    }
}

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

sealed interface AuthState {
    data object Loading : AuthState
    data class Authenticated(val user: User) : AuthState
    data object Unauthenticated : AuthState
}
