package com.vocabulary.myvocabulary.ui.user

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.user.User
import com.vocabulary.myvocabulary.repositories.user.UserRepository
import com.vocabulary.myvocabulary.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository,
    private val dictionaryRepository: DictionaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _showMobileDataWarning = MutableStateFlow(false)
    val showMobileDataWarning: StateFlow<Boolean> = _showMobileDataWarning.asStateFlow()

    val currentUser: StateFlow<User?> = userRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun onLoginClick(context: Context) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = userRepository.loginWithGoogle(context)
            if (result.isSuccess) {
                val isMobile = NetworkUtils.isMobileDataActive(context)
                val uid = userRepository.currentUserId ?: return@launch

                if (isMobile) {
                    _showMobileDataWarning.value = true
                } else {
                    // Sync immediately on Wi-Fi
                    dictionaryRepository.syncFromCloud(uid, requireWifi = false)
                    dictionaryRepository.syncAllToCloud(requireWifi = false)
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
            val uid = userRepository.currentUserId ?: return@launch
            
            // For both download and upload:
            // if proceed = true -> sync now (requireWifi = false)
            // if proceed = false -> sync later (requireWifi = true)
            dictionaryRepository.syncFromCloud(uid, requireWifi = !proceed)
            dictionaryRepository.syncAllToCloud(requireWifi = !proceed)
            
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
