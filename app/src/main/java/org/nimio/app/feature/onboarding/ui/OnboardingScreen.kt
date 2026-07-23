package org.nimio.app.feature.onboarding.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yalantis.ucrop.UCrop
import org.nimio.app.R
import kotlinx.coroutines.launch
import org.nimio.app.core.common.createSquareCropIntent
import org.nimio.app.core.common.importCroppedAvatar
import org.nimio.app.core.common.removeAvatarUri
import org.nimio.app.core.common.persistReadPermission
import org.nimio.app.core.common.releaseReadPermission
import org.nimio.app.core.ui.ProfileAvatar

@Composable
fun OnboardingScreen(
    onContinue: (displayName: String, bio: String, avatarUri: String?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<String?>(null) }
    var pendingSourceUri by remember { mutableStateOf<Uri?>(null) }
    var isPhotoProcessing by remember { mutableStateOf(false) }
    val trimmedName = name.trim()

    val cropImage = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val sourceUri = pendingSourceUri
        pendingSourceUri = null

        coroutineScope.launch {
            try {
                val oldAvatar = avatarUri
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
                    avatarUri = importedAvatar
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
                        val oldAvatar = avatarUri
                        val importedAvatar = importCroppedAvatar(context, uri)
                        if (importedAvatar != null) {
                            removeAvatarUri(context, oldAvatar)
                            avatarUri = importedAvatar
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RowBranding()

            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.onboarding_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = R.string.onboarding_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(id = R.string.account_display_name_label)) },
                        placeholder = { Text(text = stringResource(id = R.string.onboarding_name_placeholder)) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        label = { Text(text = stringResource(id = R.string.account_bio_label)) },
                        placeholder = { Text(text = stringResource(id = R.string.onboarding_bio_placeholder)) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        shape = RoundedCornerShape(14.dp)
                    )

                    AvatarPickerRow(
                        avatarUri = avatarUri,
                        fallbackName = trimmedName,
                        isPhotoProcessing = isPhotoProcessing,
                        onPick = { pickImage.launch(arrayOf("image/*")) },
                        onRemove = {
                            removeAvatarUri(context, avatarUri)
                            avatarUri = null
                        }
                    )

                    ProfilePreviewCard(
                        previewName = if (trimmedName.isNotEmpty()) trimmedName else stringResource(id = R.string.onboarding_preview_name_fallback),
                        previewBio = bio.trim().ifEmpty { stringResource(id = R.string.onboarding_preview_bio_fallback) }
                    )

                    Button(
                        onClick = { onContinue(name, bio, avatarUri) },
                        enabled = trimmedName.isNotEmpty() && !isPhotoProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = stringResource(id = R.string.onboarding_cta))
                    }
                }
            }
        }
    }
}

@Composable
private fun RowBranding() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.nimio_logo),
            contentDescription = stringResource(id = R.string.nimio_logo_content_description),
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = "  ${stringResource(id = R.string.app_name)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfilePreviewCard(
    previewName: String,
    previewBio: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(id = R.string.onboarding_preview_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                Text(
                    text = previewName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = previewBio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun AvatarPickerRow(
    avatarUri: String?,
    fallbackName: String,
    isPhotoProcessing: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfileAvatar(
            avatarUri = avatarUri,
            fallbackName = fallbackName,
            size = 54.dp,
            cornerRadius = 14.dp
        )

        Button(
            onClick = onPick,
            enabled = !isPhotoProcessing,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isPhotoProcessing) {
                    stringResource(id = R.string.photo_processing)
                } else if (avatarUri == null) {
                    stringResource(id = R.string.onboarding_pick_photo)
                } else {
                    stringResource(id = R.string.onboarding_change_photo)
                }
            )
        }

        if (avatarUri != null && !isPhotoProcessing) {
            Text(
                text = stringResource(id = R.string.onboarding_remove_photo),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .padding(2.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}

