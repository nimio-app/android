package org.nimio.app.feature.account.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yalantis.ucrop.UCrop
import org.nimio.app.R
import kotlinx.coroutines.launch
import org.nimio.app.core.common.createSquareCropIntent
import org.nimio.app.core.common.importCroppedAvatar
import org.nimio.app.core.common.persistReadPermission
import org.nimio.app.core.common.releaseReadPermission
import org.nimio.app.core.common.removeAvatarUri
import org.nimio.app.core.ui.NimioSectionCard
import org.nimio.app.core.ui.NimioSectionHeader
import org.nimio.app.core.ui.ProfileAvatar
import org.nimio.app.feature.account.domain.LocalProfileRepository

@Composable
fun AccountScreen(
    profileRepository: LocalProfileRepository
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val viewModelFactory = remember(profileRepository) {
        AccountViewModelFactory(profileRepository)
    }
    val viewModel: AccountViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingSourceUri by remember { mutableStateOf<Uri?>(null) }
    var isPhotoProcessing by remember { mutableStateOf(false) }

    val cropImage = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val sourceUri = pendingSourceUri
        pendingSourceUri = null

        coroutineScope.launch {
            try {
                val oldAvatar = uiState.avatarUri
                val importedAvatar = when {
                    result.resultCode == Activity.RESULT_OK -> {
                        val croppedUri = result.data?.let(UCrop::getOutput)
                        if (croppedUri != null) importCroppedAvatar(context, croppedUri) else null
                    }
                    sourceUri != null -> importCroppedAvatar(context, sourceUri)
                    else -> null
                }

                if (importedAvatar != null) {
                    removeAvatarUri(context, oldAvatar)
                    viewModel.onAvatarChanged(importedAvatar)
                }
            } finally {
                if (sourceUri != null) {
                    releaseReadPermission(context, sourceUri.toString())
                }
                isPhotoProcessing = false
            }
        }
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            persistReadPermission(context, uri)
            pendingSourceUri = uri
            isPhotoProcessing = true

            val launched = runCatching {
                cropImage.launch(createSquareCropIntent(context, uri))
            }.isSuccess

            if (!launched) {
                coroutineScope.launch {
                    try {
                        val oldAvatar = uiState.avatarUri
                        val importedAvatar = importCroppedAvatar(context, uri)
                        if (importedAvatar != null) {
                            removeAvatarUri(context, oldAvatar)
                            viewModel.onAvatarChanged(importedAvatar)
                        }
                    } finally {
                        releaseReadPermission(context, uri.toString())
                        pendingSourceUri = null
                        isPhotoProcessing = false
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        NimioSectionCard {
            NimioSectionHeader(
                title = stringResource(id = R.string.account_screen_title),
                description = stringResource(id = R.string.account_profile_intro)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileAvatar(
                    avatarUri = uiState.avatarUri,
                    fallbackName = uiState.displayName,
                    size = 56.dp,
                    cornerRadius = 14.dp
                )

                Button(
                    onClick = { pickImage.launch(arrayOf("image/*")) },
                    enabled = !isPhotoProcessing,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isPhotoProcessing) {
                            stringResource(id = R.string.photo_processing)
                        } else if (uiState.avatarUri == null) {
                            stringResource(id = R.string.onboarding_pick_photo)
                        } else {
                            stringResource(id = R.string.onboarding_change_photo)
                        }
                    )
                }

                if (uiState.avatarUri != null && !isPhotoProcessing) {
                    Text(
                        text = stringResource(id = R.string.onboarding_remove_photo),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            removeAvatarUri(context, uiState.avatarUri)
                            viewModel.onAvatarChanged(null)
                        }
                    )
                }
            }
        }

        NimioSectionCard {
            NimioSectionHeader(title = stringResource(id = R.string.account_profile_preview_title))
            Text(
                text = uiState.displayName.ifBlank { stringResource(id = R.string.onboarding_preview_name_fallback) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = uiState.bio.ifBlank { stringResource(id = R.string.account_profile_preview_fallback_bio) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
            )
        }

        NimioSectionCard {
            NimioSectionHeader(title = stringResource(id = R.string.account_identity_title))
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
        }

        Button(
            onClick = viewModel::saveProfile,
            enabled = uiState.displayName.trim().isNotEmpty() && !uiState.isSaving && !isPhotoProcessing,
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



