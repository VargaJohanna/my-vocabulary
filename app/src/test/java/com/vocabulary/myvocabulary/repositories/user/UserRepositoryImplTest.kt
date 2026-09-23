package com.vocabulary.myvocabulary.repositories.user

import android.content.Context
import android.util.Log
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vocabulary.myvocabulary.testing.MainCoroutineRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val firebaseUser: FirebaseUser = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        every { firebaseUser.uid } returns "user_999"
        every { firebaseUser.email } returns "user@example.com"
        every { firebaseUser.displayName } returns "Jane Doe"
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `currentUserId should return firebase user uid`() {
        every { firebaseAuth.currentUser } returns firebaseUser

        val repository = UserRepositoryImpl(firebaseAuth)

        assertThat(repository.currentUserId).isEqualTo("user_999")
    }

    @Test
    fun `currentUserId should return null when firebase user is null`() {
        every { firebaseAuth.currentUser } returns null

        val repository = UserRepositoryImpl(firebaseAuth)

        assertThat(repository.currentUserId).isNull()
    }

    @Test
    fun `currentUser flow should emit user when auth listener fires with user`() = runTest {
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseAuth.addAuthStateListener(any()) } answers {
            val listener = firstArg<FirebaseAuth.AuthStateListener>()
            listener.onAuthStateChanged(firebaseAuth)
        }

        val repository = UserRepositoryImpl(firebaseAuth)

        repository.currentUser.test {
            val user = awaitItem()
            assertThat(user?.uid).isEqualTo("user_999")
            assertThat(user?.email).isEqualTo("user@example.com")
            assertThat(user?.displayName).isEqualTo("Jane Doe")
        }
    }

    @Test
    fun `logout should sign out from firebaseAuth`() = runTest {
        val repository = UserRepositoryImpl(firebaseAuth)

        repository.logout(context)

        verify { firebaseAuth.signOut() }
    }
}
