package org.nimio.app.feature.account.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.nimio.app.core.common.NimioResult
import org.nimio.app.feature.account.domain.AccountRepository
import org.nimio.app.feature.account.domain.AccountSession

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = AuthMainDispatcherRule()

    @Test
    fun `submit in register mode calls repository register`() = runTest {
        val repository = FakeAccountRepository()
        val viewModel = AuthViewModel(repository)

        viewModel.onDisplayNameChanged("Lakky")
        viewModel.onUsernameChanged("lakky")
        viewModel.onEmailChanged("lakky@example.com")
        viewModel.onPasswordChanged("SecurePass123")
        viewModel.submit()

        advanceUntilIdle()

        assertEquals(1, repository.registerCalls)
        assertEquals(0, repository.signInCalls)
        assertEquals("lakky@example.com", repository.lastRegisterEmail)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertTrue(viewModel.uiState.value.errorMessage == null)
    }

    @Test
    fun `submit in login mode calls repository signIn`() = runTest {
        val repository = FakeAccountRepository()
        val viewModel = AuthViewModel(repository)

        viewModel.onModeChanged(true)
        viewModel.onEmailChanged("lakky@example.com")
        viewModel.onPasswordChanged("SecurePass123")
        viewModel.submit()

        advanceUntilIdle()

        assertEquals(0, repository.registerCalls)
        assertEquals(1, repository.signInCalls)
        assertEquals("lakky@example.com", repository.lastLoginEmail)
    }

    @Test
    fun `submit surfaces repository error message`() = runTest {
        val repository = FakeAccountRepository(
            registerResult = NimioResult.Error(IllegalStateException("email already taken"))
        )
        val viewModel = AuthViewModel(repository)

        viewModel.onDisplayNameChanged("Lakky")
        viewModel.onUsernameChanged("lakky")
        viewModel.onEmailChanged("lakky@example.com")
        viewModel.onPasswordChanged("SecurePass123")
        viewModel.submit()

        advanceUntilIdle()

        assertEquals("email already taken", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    private class FakeAccountRepository(
        private val registerResult: NimioResult<AccountSession> = NimioResult.Success(
            AccountSession("1", "Lakky", true)
        ),
        private val loginResult: NimioResult<AccountSession> = NimioResult.Success(
            AccountSession("1", "Lakky", true)
        )
    ) : AccountRepository {
        private val session = MutableStateFlow<AccountSession?>(null)
        var registerCalls = 0
        var signInCalls = 0
        var lastRegisterEmail = ""
        var lastLoginEmail = ""

        override fun observeSession(): Flow<AccountSession?> = session.asStateFlow()

        override suspend fun register(
            email: String,
            password: String,
            username: String,
            displayName: String
        ): NimioResult<AccountSession> {
            registerCalls++
            lastRegisterEmail = email
            return registerResult
        }

        override suspend fun signIn(
            email: String,
            password: String
        ): NimioResult<AccountSession> {
            signInCalls++
            lastLoginEmail = email
            return loginResult
        }

        override suspend fun googleSignIn(idToken: String): NimioResult<AccountSession> {
            return loginResult
        }

        override suspend fun resendVerification(email: String): NimioResult<Unit> {
            return NimioResult.Success(Unit)
        }

        override suspend fun verifyEmailToken(token: String): NimioResult<Unit> {
            return NimioResult.Success(Unit)
        }

        override suspend fun updateProfile(
            username: String,
            displayName: String,
            bio: String
        ): NimioResult<Unit> {
            return NimioResult.Success(Unit)
        }

        override suspend fun refreshSession(): NimioResult<AccountSession?> {
            return NimioResult.Success(session.value)
        }

        override suspend fun signOut() {
            session.value = null
        }

        override suspend fun uploadAvatar(filePath: String): NimioResult<String> {
            return NimioResult.Success("https://example.com/avatars/test.jpg")
        }

        override suspend fun deleteAvatar(): NimioResult<Unit> {
            return NimioResult.Success(Unit)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthMainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

