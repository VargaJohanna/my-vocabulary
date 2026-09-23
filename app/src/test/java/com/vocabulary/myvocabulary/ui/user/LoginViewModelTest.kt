package com.vocabulary.myvocabulary.ui.user

import android.content.Context
import android.util.Log
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.user.User
import com.vocabulary.myvocabulary.repositories.user.UserRepository
import com.vocabulary.myvocabulary.testing.MainCoroutineRule
import com.vocabulary.myvocabulary.utils.NetworkUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val dictionaryRepository: DictionaryRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private val currentUserFlow = MutableStateFlow<User?>(null)
    private val testUser = User(uid = "user_123", email = "test@example.com", displayName = "Test User")

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        mockkObject(NetworkUtils)
        every { userRepository.currentUser } returns currentUserFlow
        every { userRepository.currentUserId } returns "user_123"
    }

    @After
    fun tearDown() {
        unmockkObject(NetworkUtils)
        unmockkStatic(Log::class)
    }

    @Test
    fun `authState should transition to Unauthenticated when user is null`() = runTest {
        val viewModel = LoginViewModel(userRepository, dictionaryRepository, TestScope(mainCoroutineRule.testDispatcher))

        viewModel.authState.test {
            assertThat(awaitItem()).isInstanceOf(AuthState.Loading::class)
            assertThat(awaitItem()).isInstanceOf(AuthState.Unauthenticated::class)
        }
    }

    @Test
    fun `authState should transition to Authenticated when user is present`() = runTest {
        currentUserFlow.value = testUser
        val viewModel = LoginViewModel(userRepository, dictionaryRepository, TestScope(mainCoroutineRule.testDispatcher))

        viewModel.authState.test {
            assertThat(awaitItem()).isInstanceOf(AuthState.Loading::class)
            val state = awaitItem()
            assertThat(state).isInstanceOf(AuthState.Authenticated::class)
            assertThat((state as AuthState.Authenticated).user).isEqualTo(testUser)
        }
    }

    @Test
    fun `onLoginClick should perform sync and set Success when on Wi-Fi`() = runTest {
        coEvery { userRepository.loginWithGoogle(context) } returns Result.success(Unit)
        every { NetworkUtils.isMobileDataActive(context) } returns false
        coEvery { dictionaryRepository.syncFromCloud("user_123", requireWifi = false) } returns Result.success(Unit)

        val viewModel = LoginViewModel(userRepository, dictionaryRepository, TestScope(mainCoroutineRule.testDispatcher))

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(LoginUiState.Idle)

            viewModel.onLoginClick(context)

            assertThat(awaitItem()).isEqualTo(LoginUiState.Loading)
            assertThat(awaitItem()).isEqualTo(LoginUiState.Success)
        }

        coVerify { dictionaryRepository.syncFromCloud("user_123", requireWifi = false) }
        coVerify { dictionaryRepository.syncAllToCloud(requireWifi = false) }
    }

    @Test
    fun `onLoginClick should show mobile data warning when on cellular data`() = runTest {
        coEvery { userRepository.loginWithGoogle(context) } returns Result.success(Unit)
        every { NetworkUtils.isMobileDataActive(context) } returns true

        val viewModel = LoginViewModel(userRepository, dictionaryRepository, TestScope(mainCoroutineRule.testDispatcher))

        viewModel.showMobileDataWarning.test {
            assertThat(awaitItem()).isFalse()

            viewModel.onLoginClick(context)

            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `onLoginClick should set Error state when login fails`() = runTest {
        coEvery { userRepository.loginWithGoogle(context) } returns Result.failure(Exception("Auth Failed"))

        val viewModel = LoginViewModel(userRepository, dictionaryRepository, TestScope(mainCoroutineRule.testDispatcher))

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(LoginUiState.Idle)

            viewModel.onLoginClick(context)

            assertThat(awaitItem()).isEqualTo(LoginUiState.Loading)
            val state = awaitItem()
            assertThat(state).isInstanceOf(LoginUiState.Error::class)
            assertThat((state as LoginUiState.Error).message).isEqualTo("Auth Failed")
        }
    }

    @Test
    fun `onConfirmSync should proceed with sync and set Success state`() = runTest {
        coEvery { dictionaryRepository.syncFromCloud("user_123", requireWifi = false) } returns Result.success(Unit)

        val viewModel = LoginViewModel(userRepository, dictionaryRepository, TestScope(mainCoroutineRule.testDispatcher))

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(LoginUiState.Idle)

            viewModel.onConfirmSync(proceed = true)

            assertThat(awaitItem()).isEqualTo(LoginUiState.Loading)
            assertThat(awaitItem()).isEqualTo(LoginUiState.Success)
        }

        assertThat(viewModel.showMobileDataWarning.value).isFalse()
        coVerify { dictionaryRepository.syncFromCloud("user_123", requireWifi = false) }
    }

    @Test
    fun `onLogoutClick should clear local data and logout user`() = runTest {
        val viewModel = LoginViewModel(userRepository, dictionaryRepository, TestScope(mainCoroutineRule.testDispatcher))

        viewModel.onLogoutClick(context)
        advanceUntilIdle()

        coVerify { dictionaryRepository.clearLocalData() }
        coVerify { userRepository.logout(context) }
    }
}
