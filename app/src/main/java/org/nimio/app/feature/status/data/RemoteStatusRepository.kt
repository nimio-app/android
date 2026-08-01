package org.nimio.app.feature.status.data

import kotlinx.coroutines.flow.Flow
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

    override fun observeStatus(): Flow<UserStatus> = dataSource.observeStatus()

    override suspend fun saveStatus(status: UserStatus) {
        val request = UpdateStatusRequestDto(
            availabilityType = status.availability.toApiAvailability(),
            note = status.note,
            visibilityTier = status.visibilityTier.name,
            expiresAt = status.expiresAtEpochMillis?.let(::toIsoOrNull)
        )

        val response = api.updateMyStatus(request)
        val remote = response.data?.status

        dataSource.saveStatus(
            if (remote != null) remote.toUserStatus() else status
        )
    }

    override suspend fun refreshStatus() {
        val response = api.getMyStatus()
        val remote = response.data?.status ?: return
        dataSource.saveStatus(remote.toUserStatus())
    }

    override suspend fun clearStatus() {
        api.clearMyStatus()
        dataSource.saveStatus(UserStatus())
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

