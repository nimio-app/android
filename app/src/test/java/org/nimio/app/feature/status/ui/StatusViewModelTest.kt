package org.nimio.app.feature.status.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
import org.nimio.app.feature.status.domain.Availability
import org.nimio.app.feature.status.domain.StatusRepository
import org.nimio.app.feature.status.domain.UserStatus

@OptIn(ExperimentalCoroutinesApi::class)
class StatusViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `saveStatus persists selected availability and trimmed activity`() = runTest {
        val repository = FakeStatusRepository()
        val viewModel = StatusViewModel(repository = repository)

        viewModel.onAvailabilitySelected(Availability.FOCUSED)
        viewModel.onActivityChanged("  heads down on architecture doc  ")
        viewModel.saveStatus()

        advanceUntilIdle()

        val savedStatus = repository.observeStatus().first()
        assertEquals(Availability.FOCUSED, savedStatus.availability)
        assertEquals("heads down on architecture doc", savedStatus.activity)
        assertTrue(savedStatus.updatedAtEpochMillis != null)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    private class FakeStatusRepository : StatusRepository {
        private val state = MutableStateFlow(UserStatus())

        override fun observeStatus(): Flow<UserStatus> = state

        override suspend fun saveStatus(status: UserStatus) {
            state.value = status
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}


