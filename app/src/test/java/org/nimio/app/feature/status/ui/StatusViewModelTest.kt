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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.nimio.app.feature.status.domain.Availability
import org.nimio.app.feature.status.domain.StatusExpiry
import org.nimio.app.feature.status.domain.StatusRepository
import org.nimio.app.feature.status.domain.UserStatus
import org.nimio.app.feature.status.sync.StatusExpiryScheduler

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

    @Test
    fun `saveStatus schedules expiry worker when a timed expiry is selected`() = runTest {
        val repository = FakeStatusRepository()
        val scheduler = FakeStatusExpiryScheduler()
        val viewModel = StatusViewModel(repository = repository, expiryScheduler = scheduler)

        viewModel.onExpirySelected(StatusExpiry.ONE_HOUR)
        viewModel.saveStatus()

        advanceUntilIdle()

        assertTrue("expected schedule() to be called", scheduler.scheduleCallCount == 1)
        assertTrue("expected delay to be positive", scheduler.lastScheduledDelayMillis!! > 0)
        assertEquals(0, scheduler.cancelCallCount)
    }

    @Test
    fun `saveStatus cancels expiry worker when NONE expiry is selected`() = runTest {
        val repository = FakeStatusRepository()
        val scheduler = FakeStatusExpiryScheduler()
        val viewModel = StatusViewModel(repository = repository, expiryScheduler = scheduler)

        viewModel.onExpirySelected(StatusExpiry.NONE)
        viewModel.saveStatus()

        advanceUntilIdle()

        assertEquals(1, scheduler.cancelCallCount)
        assertEquals(0, scheduler.scheduleCallCount)
    }

    @Test
    fun `saveStatus persists expiresAtEpochMillis for timed expiry`() = runTest {
        val repository = FakeStatusRepository()
        val viewModel = StatusViewModel(repository = repository)

        viewModel.onExpirySelected(StatusExpiry.THIRTY_MIN)
        viewModel.saveStatus()

        advanceUntilIdle()

        val saved = repository.observeStatus().first()
        assertTrue("expiresAtEpochMillis should be set", saved.expiresAtEpochMillis != null)
        assertTrue(
            "expiresAt should be roughly 30 min ahead",
            saved.expiresAtEpochMillis!! > System.currentTimeMillis()
        )
    }

    @Test
    fun `saveStatus does not persist expiresAtEpochMillis for NONE expiry`() = runTest {
        val repository = FakeStatusRepository()
        val viewModel = StatusViewModel(repository = repository)

        viewModel.onExpirySelected(StatusExpiry.NONE)
        viewModel.saveStatus()

        advanceUntilIdle()

        val saved = repository.observeStatus().first()
        assertNull(saved.expiresAtEpochMillis)
    }

    // ── Fakes ───────────────────────────────────────────────────────────────

    private class FakeStatusRepository : StatusRepository {
        private val state = MutableStateFlow(UserStatus())

        override fun observeStatus(): Flow<UserStatus> = state

        override suspend fun saveStatus(status: UserStatus) {
            state.value = status
        }
    }

    private class FakeStatusExpiryScheduler : StatusExpiryScheduler {
        var scheduleCallCount = 0
        var cancelCallCount = 0
        var lastScheduledDelayMillis: Long? = null

        override fun schedule(delayMillis: Long) {
            scheduleCallCount++
            lastScheduledDelayMillis = delayMillis
        }

        override fun cancel() {
            cancelCallCount++
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
