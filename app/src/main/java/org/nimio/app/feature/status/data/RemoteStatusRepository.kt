package org.nimio.app.feature.status.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import org.nimio.app.feature.status.domain.Availability
import org.nimio.app.feature.status.domain.StatusRepository
import org.nimio.app.feature.status.domain.UserStatus
import org.nimio.app.feature.status.domain.VisibilityTier
import java.time.Instant
import javax.inject.Inject

class RemoteStatusRepository @Inject constructor(
    private val api: StatusApi,
    private val dataSource: StatusPreferencesDataSource
) : StatusRepository {

    private val activeStatusesState = MutableStateFlow<Map<VisibilityTier, UserStatus>>(emptyMap())

    override fun observeStatus(): Flow<UserStatus> = dataSource.observeStatus()

    override fun observeActiveStatuses(): Flow<Map<VisibilityTier, UserStatus>> = activeStatusesState.asStateFlow()

    override suspend fun saveStatus(status: UserStatus) {
        val request = UpdateStatusRequestDto(
            availabilityType = status.availability.toApiAvailability(),
            note = status.note,
            visibilityTier = status.visibilityTier.name,
            expiresAt = status.expiresAtEpochMillis?.let(::toIsoOrNull)
        )

        val response = api.updateMyStatus(request)
        val remote = response.data?.status

        val resolved = if (remote != null) remote.toUserStatus() else status
        dataSource.saveStatus(resolved)

        refreshStatus()
    }

    override suspend fun refreshStatus() {
        val statuses = runCatching {
            api.getMyStatuses().data?.statuses.orEmpty()
        }.getOrNull()

        if (statuses != null) {
            val mapped = statuses
                .map { it.toUserStatus() }
                .associateBy { it.visibilityTier }
            activeStatusesState.value = mapped

            val draftTier = dataSource.observeStatus().first().visibilityTier
            mapped[draftTier]?.let { draftStatus ->
                dataSource.saveStatus(draftStatus)
            }
            return
        }

        // Keep compatibility for deployments that might not support /v1/me/statuses yet.
        runCatching {
            api.getMyStatus().data?.status
        }.getOrNull()?.toUserStatus()?.let { fallback ->
            activeStatusesState.value = mapOf(fallback.visibilityTier to fallback)
            dataSource.saveStatus(fallback)
        }
    }

    override suspend fun clearStatus() {
        api.clearMyStatus()
        dataSource.saveStatus(UserStatus())
        activeStatusesState.value = emptyMap()
    }

    private fun StatusDto.toUserStatus(): UserStatus {
        return UserStatus(
            availability = availabilityType.toAvailability(),
            note = note.orEmpty(),
            visibilityTier = visibilityTier.toVisibilityTier(),
            updatedAtEpochMillis = createdAt?.let(::toEpochMillisOrNull),
            expiresAtEpochMillis = expiresAt?.let(::toEpochMillisOrNull)
        )
    }

    private fun String.toAvailability(): Availability {
        return runCatching { Availability.valueOf(this) }.getOrDefault(Availability.FREE)
    }

    private fun Availability.toApiAvailability(): String = name

    private fun String.toVisibilityTier(): VisibilityTier {
        return runCatching { VisibilityTier.valueOf(this) }
            .getOrDefault(VisibilityTier.ALL_CONNECTIONS)
    }

    private fun toIsoOrNull(epoch: Long): String? {
        return runCatching { Instant.ofEpochMilli(epoch).toString() }.getOrNull()
    }

    private fun toEpochMillisOrNull(iso: String): Long? {
        return runCatching { Instant.parse(iso).toEpochMilli() }.getOrNull()
    }
}

