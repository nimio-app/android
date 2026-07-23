package org.nimio.app.feature.status.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.nimio.app.R
import org.nimio.app.core.ui.NimioSectionCard
import org.nimio.app.core.ui.NimioSectionHeader
import org.nimio.app.feature.status.data.DefaultStatusRepository
import org.nimio.app.feature.status.data.StatusPreferencesDataSource
import org.nimio.app.feature.status.domain.Availability
import org.nimio.app.feature.status.domain.StatusExpiry
import org.nimio.app.feature.status.sync.WorkManagerStatusExpiryScheduler

@Composable
fun StatusScreen() {
    val context = LocalContext.current
    val viewModelFactory = androidx.compose.runtime.remember(context) {
        StatusViewModelFactory(
            repository = DefaultStatusRepository(
                dataSource = StatusPreferencesDataSource(context)
            ),
            expiryScheduler = WorkManagerStatusExpiryScheduler(context)
        )
    }
    val viewModel: StatusViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current state summary header
        CurrentStatusSummary(uiState = uiState)

        NimioSectionCard {
            NimioSectionHeader(
                title = stringResource(id = R.string.status_section_feeling_title),
                description = stringResource(id = R.string.status_section_feeling_description)
            )
            AvailabilityStateGrid(
                selected = uiState.selectedAvailability,
                onSelected = viewModel::onAvailabilitySelected
            )
        }

        NimioSectionCard {
            NimioSectionHeader(
                title = stringResource(id = R.string.status_note_label)
            )
            OutlinedTextField(
                value = uiState.noteText,
                onValueChange = viewModel::onNoteChanged,
                label = { Text(stringResource(id = R.string.status_note_label)) },
                placeholder = { Text(stringResource(id = R.string.status_note_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
        }

        NimioSectionCard {
            NimioSectionHeader(
                title = stringResource(id = R.string.status_expiry_title),
                description = stringResource(id = R.string.status_expiry_description)
            )
            ExpirySelector(
                selected = uiState.selectedExpiry,
                onSelected = viewModel::onExpirySelected
            )
        }

        ElevatedButton(
            onClick = viewModel::saveStatus,
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = if (uiState.justSaved) {
                    stringResource(id = R.string.status_saved)
                } else {
                    stringResource(id = R.string.status_save_button)
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CurrentStatusSummary(uiState: StatusUiState) {
    val current = uiState.selectedAvailability
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = current.cardColor
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = current.emoji,
                style = MaterialTheme.typography.displaySmall
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = current.displayLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = current.hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                if (uiState.noteText.isNotBlank()) {
                    Text(
                        text = "\"${uiState.noteText}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AvailabilityStateGrid(
    selected: Availability,
    onSelected: (Availability) -> Unit
) {
    val states = Availability.entries
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        states.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { availability ->
                    AvailabilityStateCard(
                        availability = availability,
                        isSelected = availability == selected,
                        onClick = { onSelected(availability) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // fill last row if odd count
                if (row.size == 1) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AvailabilityStateCard(
    availability: Availability,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) availability.cardColor else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = availability.emoji, style = MaterialTheme.typography.titleLarge)
            Text(
                text = availability.displayLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = availability.hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ExpirySelector(
    selected: StatusExpiry,
    onSelected: (StatusExpiry) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(StatusExpiry.entries) { expiry ->
            FilterChip(
                selected = expiry == selected,
                onClick = { onSelected(expiry) },
                label = { Text(text = expiry.displayLabel) }
            )
        }
    }
}
