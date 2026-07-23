package org.nimio.app.feature.account.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.nimio.app.R
import org.nimio.app.feature.account.domain.LocalProfileRepository
import org.nimio.app.core.ui.PlaceholderScreenCard

@Composable
fun AccountScreen(
    profileRepository: LocalProfileRepository
) {
    val viewModelFactory = remember(profileRepository) {
        AccountViewModelFactory(profileRepository)
    }
    val viewModel: AccountViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PlaceholderScreenCard(
            title = stringResource(id = R.string.account_screen_title),
            description = stringResource(id = R.string.account_screen_description)
        )
        PlaceholderScreenCard(
            title = stringResource(id = R.string.account_screen_title),
            description = stringResource(id = R.string.account_profile_intro)
        )

        OutlinedTextField(
            value = uiState.displayName,
            onValueChange = viewModel::onDisplayNameChanged,
            label = { Text(text = stringResource(id = R.string.account_display_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )

        OutlinedTextField(
            value = uiState.bio,
            onValueChange = viewModel::onBioChanged,
            label = { Text(text = stringResource(id = R.string.account_bio_label)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        Button(
            onClick = viewModel::saveProfile,
            enabled = uiState.displayName.trim().isNotEmpty() && !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (uiState.saved) {
                    stringResource(id = R.string.account_profile_saved)
                } else {
                    stringResource(id = R.string.account_save_profile)
                }
            )
        }

        Text(
            text = stringResource(id = R.string.sync_status_pending),
            style = MaterialTheme.typography.labelLarge
        )
    }
}



