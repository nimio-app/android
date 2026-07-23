package org.nimio.app.feature.account.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import org.nimio.app.R
import org.nimio.app.feature.account.domain.LocalProfileRepository

@Composable
fun AccountScreen(
    profileRepository: LocalProfileRepository
) {
    val viewModelFactory = remember(profileRepository) {
        AccountViewModelFactory(profileRepository)
    }
    val viewModel: AccountViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.onAvatarChanged(uri?.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.account_screen_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = R.string.account_profile_intro),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.avatarUri != null) {
                        AsyncImage(
                            model = uiState.avatarUri,
                            contentDescription = stringResource(id = R.string.account_avatar_content_description),
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.nimio_logo),
                            contentDescription = stringResource(id = R.string.nimio_logo_content_description),
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Button(
                        onClick = { pickImage.launch("image/*") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (uiState.avatarUri == null) {
                                stringResource(id = R.string.onboarding_pick_photo)
                            } else {
                                stringResource(id = R.string.onboarding_change_photo)
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = uiState.displayName,
            onValueChange = viewModel::onDisplayNameChanged,
            label = { Text(text = stringResource(id = R.string.account_display_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )

        OutlinedTextField(
            value = uiState.bio,
            onValueChange = viewModel::onBioChanged,
            label = { Text(text = stringResource(id = R.string.account_bio_label)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        Button(
            onClick = viewModel::saveProfile,
            enabled = uiState.displayName.trim().isNotEmpty() && !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
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



