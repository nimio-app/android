package org.nimio.app.feature.social.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import org.nimio.app.R
import org.nimio.app.feature.social.data.InMemorySocialGraphRepository
import org.nimio.app.feature.social.domain.ConnectionStatus
import org.nimio.app.feature.social.domain.ConnectionTier
import org.nimio.app.feature.social.domain.SocialGraphRepository
import org.nimio.app.feature.social.domain.UserSearchResult

@Composable
fun SocialGraphScreen(
    socialGraphRepository: SocialGraphRepository = InMemorySocialGraphRepository()
) {
    val factory = remember(socialGraphRepository) { SocialGraphViewModelFactory(socialGraphRepository) }
    val viewModel: SocialGraphViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Text(
            text = stringResource(id = R.string.social_screen_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(id = R.string.social_connections_count, uiState.count),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(id = R.string.social_directional_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        TierLegend()

        Spacer(modifier = Modifier.height(16.dp))

        // Single search field — does double duty: filters the local list AND searches users
        OutlinedTextField(
            value = uiState.requestSearchQuery.ifBlank { uiState.searchQuery },
            onValueChange = { query ->
                // If the field has content from the search-users path, keep using that; else switch to local filter
                if (uiState.requestSearchQuery.isNotBlank() || query.isNotBlank()) {
                    viewModel.onRequestSearchQueryChanged(query)
                    viewModel.onSearchQueryChanged(query)
                } else {
                    viewModel.onSearchQueryChanged(query)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(id = R.string.social_search_placeholder)) },
            leadingIcon = {
                if (uiState.isSearchingUsers) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status/error feedback strip
        uiState.message?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        }
        uiState.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {

            // ── User search results (when query typed) ──────────────────────
            val query = uiState.requestSearchQuery.trim()
            if (query.length >= 2) {
                if (uiState.searchResults.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(id = R.string.social_people_title),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(uiState.searchResults) { result ->
                        val existingStatus = uiState.connectionStatusByUserId[result.userId]
                            ?: if (uiState.pendingOutgoingUserIds.contains(result.userId)) ConnectionStatus.PENDING else null
                        SearchPersonRow(
                            result = result,
                            existingStatus = existingStatus,
                            selectedTier = uiState.relationshipTier,
                            onTierChanged = viewModel::onRelationshipTierChanged,
                            onRequest = { viewModel.sendRequest(result.userId) },
                            isSubmitting = uiState.isSubmitting
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    }
                } else if (!uiState.isSearchingUsers) {
                    item {
                        Text(
                            text = stringResource(id = R.string.social_search_empty, query),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }

            // ── Visible status feed ──────────────────────────────────────────
            if (query.length < 2) {
                item {
                    Text(
                        text = stringResource(id = R.string.social_statuses_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (uiState.visibleStatuses.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(id = R.string.social_statuses_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                } else {
                    items(uiState.visibleStatuses) { status ->
                        StatusFeedRow(
                            title = status.displayName.ifBlank { status.username },
                            username = status.username,
                            avatarUrl = status.avatarUrl,
                            availability = status.availabilityType,
                            note = status.note
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    }
                }
            }

            // ── Pending requests split by direction ─────────────────────────
            val incomingPending = uiState.incomingPendingRequests
            val outgoingPending = uiState.outgoingPendingRequests
            if ((incomingPending.isNotEmpty() || outgoingPending.isNotEmpty()) && query.length < 2) {
                val pendingTotal = incomingPending.size + outgoingPending.size
                item {
                    Text(
                        text = stringResource(id = R.string.social_requests_count, pendingTotal),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                if (incomingPending.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(id = R.string.social_incoming_count, incomingPending.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
                items(incomingPending) { item ->
                    ConnectionPersonRow(
                        title = item.displayName.ifBlank { item.username },
                        username = item.username,
                        avatarUrl = item.avatarUrl,
                        trailing = {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { viewModel.accept(item.counterpartUserId) },
                                    enabled = !uiState.isSubmitting
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = stringResource(id = R.string.social_action_accept_content_description),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                TextButton(
                                    onClick = { viewModel.reject(item.counterpartUserId) },
                                    enabled = !uiState.isSubmitting
                                ) { Text(stringResource(id = R.string.social_decline)) }
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                }

                if (outgoingPending.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(id = R.string.social_sent_count, outgoingPending.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                        )
                    }
                }
                items(outgoingPending) { item ->
                    ConnectionPersonRow(
                        title = item.displayName.ifBlank { item.username },
                        username = item.username,
                        avatarUrl = item.avatarUrl,
                        trailing = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(
                                    label = stringResource(id = R.string.social_badge_sent),
                                    container = MaterialTheme.colorScheme.secondaryContainer,
                                    content = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                TextButton(
                                    onClick = { viewModel.remove(item.counterpartUserId) },
                                    enabled = !uiState.isSubmitting
                                ) { Text(stringResource(id = R.string.social_cancel)) }
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                }
            }

            // ── Accepted connections ────────────────────────────────────────
            val acceptedList = uiState.filteredConnections.filter { it.status == ConnectionStatus.ACCEPTED }
            if (acceptedList.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.social_people_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(acceptedList) { item ->
                    ConnectionPersonRow(
                        title = item.displayName.ifBlank { item.username },
                        username = item.username,
                        avatarUrl = item.avatarUrl,
                        myTierForThem = item.myTierForThem,
                        trailing = {
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ConnectionTier.entries.forEach { tier ->
                                        FilterChip(
                                            selected = item.myTierForThem == tier,
                                            onClick = { viewModel.updateTier(item.id, tier) },
                                            label = { Text(tier.displayLabel) },
                                            enabled = !uiState.isSubmitting
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = { viewModel.remove(item.counterpartUserId) },
                                    enabled = !uiState.isSubmitting
                                ) { Text(stringResource(id = R.string.social_remove)) }
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                }
            }

            // ── Empty state ─────────────────────────────────────────────────
            if (uiState.connections.isEmpty() && query.length < 2) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(id = R.string.social_empty_state_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusFeedRow(
    title: String,
    username: String,
    avatarUrl: String?,
    availability: String,
    note: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(avatarUrl = avatarUrl, name = title)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = "@$username", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = availability.replace('_', ' '), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            if (note.isNotBlank()) {
                Text(text = note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun TierLegend() {
    Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TierLegendChip(
            icon = Icons.Default.Groups,
            label = stringResource(id = R.string.social_tier_all),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TierLegendChip(
            icon = Icons.Default.Star,
            label = stringResource(id = R.string.social_tier_circle),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun TierLegendChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ── Shared avatar composable ────────────────────────────────────────────────

@Composable
private fun UserAvatar(avatarUrl: String?, name: String, size: Int = 40) {
    if (!avatarUrl.isNullOrBlank()) {
        SubcomposeAsyncImage(
            model = avatarUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size.dp).clip(CircleShape),
            loading = { InitialsAvatar(name, size) },
            error = { InitialsAvatar(name, size) }
        )
    } else {
        InitialsAvatar(name, size)
    }
}

@Composable
private fun InitialsAvatar(name: String, size: Int = 40) {
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

// ── Search result row (with tier picker before requesting) ──────────────────

@Composable
private fun SearchPersonRow(
    result: UserSearchResult,
    existingStatus: ConnectionStatus?,
    selectedTier: ConnectionTier,
    onTierChanged: (ConnectionTier) -> Unit,
    onRequest: () -> Unit,
    isSubmitting: Boolean
) {
    val title = result.displayName.ifBlank { result.username }
    var showTierPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(avatarUrl = result.avatarUrl, name = title)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("@${result.username}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            when (existingStatus) {
                ConnectionStatus.PENDING -> StatusBadge(stringResource(id = R.string.social_badge_pending), MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                ConnectionStatus.ACCEPTED -> StatusBadge(stringResource(id = R.string.social_badge_connected), MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                ConnectionStatus.BLOCKED -> StatusBadge(stringResource(id = R.string.social_badge_blocked), MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                else -> {
                    if (!showTierPicker) {
                        androidx.compose.material3.FilledTonalButton(
                            onClick = { showTierPicker = true },
                            enabled = !isSubmitting
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(stringResource(id = R.string.social_add))
                        }
                    }
                }
            }
        }
        // Tier picker expands inline, then user taps Send
        if (showTierPicker && existingStatus == null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 52.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConnectionTier.entries.forEach { tier ->
                    FilterChip(
                        selected = selectedTier == tier,
                        onClick = { onTierChanged(tier) },
                        label = { Text(tier.displayLabel, style = MaterialTheme.typography.labelSmall) }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                androidx.compose.material3.Button(
                    onClick = {
                        showTierPicker = false
                        onRequest()
                    },
                    enabled = !isSubmitting
                ) {
                    Text(
                        text = if (isSubmitting) {
                            stringResource(id = R.string.social_sending_short)
                        } else {
                            stringResource(id = R.string.social_send)
                        }
                    )
                }
            }
        }
    }
}

// ── Connection row for existing friends / pending ──────────────────────────

@Composable
private fun ConnectionPersonRow(
    title: String,
    username: String,
    avatarUrl: String?,
    myTierForThem: ConnectionTier? = null,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(avatarUrl = avatarUrl, name = title)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("@$username", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (myTierForThem != null) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TierDirectionBadge(label = stringResource(id = R.string.social_tier_you), tier = myTierForThem)
                }
            }
        }
        trailing()
    }
}

@Composable
private fun TierDirectionBadge(
    label: String,
    tier: ConnectionTier
) {
    val icon = if (tier == ConnectionTier.CIRCLE) Icons.Default.Star else Icons.Default.Groups
    val tint = if (tier == ConnectionTier.CIRCLE) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = tier.displayLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ── Tiny badge ─────────────────────────────────────────────────────────────

@Composable
private fun StatusBadge(label: String, container: androidx.compose.ui.graphics.Color, content: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = container) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = content
        )
    }
}

