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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import org.nimio.app.feature.social.domain.PendingActionHint
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
            text = "${uiState.count} connections",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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
            placeholder = { Text("Search or add people…") },
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
                            text = "People",
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
                            text = "No people found for \"$query\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
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
                        text = "Requests ($pendingTotal)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                if (incomingPending.isNotEmpty()) {
                    item {
                        Text(
                            text = "Incoming (${incomingPending.size})",
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
                                        contentDescription = "Accept",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                TextButton(
                                    onClick = { viewModel.reject(item.counterpartUserId) },
                                    enabled = !uiState.isSubmitting
                                ) { Text("Decline") }
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                }

                if (outgoingPending.isNotEmpty()) {
                    item {
                        Text(
                            text = "Sent (${outgoingPending.size})",
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
                                    label = "Sent",
                                    container = MaterialTheme.colorScheme.secondaryContainer,
                                    content = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                TextButton(
                                    onClick = { viewModel.remove(item.counterpartUserId) },
                                    enabled = !uiState.isSubmitting
                                ) { Text("Cancel") }
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
                        text = "Connections",
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
                        trailing = {
                            OutlinedButton(
                                onClick = { viewModel.remove(item.counterpartUserId) },
                                enabled = !uiState.isSubmitting
                            ) { Text("Remove") }
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
                                text = "Search for people above to add friends",
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
                ConnectionStatus.PENDING -> StatusBadge("Pending", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                ConnectionStatus.ACCEPTED -> StatusBadge("Connected", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                ConnectionStatus.BLOCKED -> StatusBadge("Blocked", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                else -> {
                    if (!showTierPicker) {
                        androidx.compose.material3.FilledTonalButton(
                            onClick = { showTierPicker = true },
                            enabled = !isSubmitting
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Add")
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
                    Text(if (isSubmitting) "…" else "Send")
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
        }
        trailing()
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

