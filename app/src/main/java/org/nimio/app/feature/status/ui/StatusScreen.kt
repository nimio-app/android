package org.nimio.app.feature.status.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.nimio.app.R
import org.nimio.app.feature.status.data.DefaultStatusRepository
import org.nimio.app.feature.status.data.StatusPreferencesDataSource
import org.nimio.app.feature.status.domain.Availability
import java.util.Date

@Composable
fun StatusScreen() {
    val context = LocalContext.current
    val viewModelFactory = remember(context) {
        StatusViewModelFactory(
            repository = DefaultStatusRepository(
                dataSource = StatusPreferencesDataSource(context)
            )
        )
    }
    val viewModel: StatusViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.status_screen_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(id = R.string.status_screen_description),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Card(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvailabilitySelector(
                    selected = uiState.selectedAvailability,
                    onSelected = viewModel::onAvailabilitySelected
                )

                OutlinedTextField(
                    value = uiState.activityText,
                    onValueChange = viewModel::onActivityChanged,
                    label = { Text(text = stringResource(id = R.string.status_activity_label)) },
                    placeholder = { Text(text = stringResource(id = R.string.status_activity_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.sync_status_pending),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Button(
                        onClick = viewModel::saveStatus,
                        enabled = !uiState.isSaving
                    ) {
                        Text(text = stringResource(id = R.string.status_save_button))
                    }
                }
            }
        }

        uiState.lastUpdatedEpochMillis?.let { timestamp ->
            val formattedDate = remember(timestamp) {
                DateFormat.getMediumDateFormat(context).format(Date(timestamp)) +
                    " " +
                    DateFormat.getTimeFormat(context).format(Date(timestamp))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.status_last_updated, formattedDate),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun AvailabilitySelector(
    selected: Availability,
    onSelected: (Availability) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Availability.entries.forEach { availability ->
            FilterChip(
                selected = availability == selected,
                onClick = { onSelected(availability) },
                label = { Text(text = stringResource(id = availability.labelResId)) }
            )
        }
    }
}
