package org.nimio.app.feature.account.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.nimio.app.R

private val VerificationBannerContainer = Color(0xFFFFECB3)
private val VerificationBannerContent = Color(0xFF5D4037)

@Composable
fun VerificationBanner(
    uiState: EmailVerificationBannerUiState,
    onResendClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!uiState.shouldShow) return

    Surface(
        color = VerificationBannerContainer,
        contentColor = VerificationBannerContent,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.verification_banner_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(
                    onClick = onResendClick,
                    enabled = !uiState.isResending && uiState.cooldownSecondsRemaining == 0,
                    modifier = Modifier.padding(0.dp)
                ) {
                    if (uiState.isResending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = stringResource(id = R.string.verification_banner_sending),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    } else {
                        val label = if (uiState.cooldownSecondsRemaining > 0) {
                            stringResource(
                                id = R.string.verification_banner_resend_cooldown,
                                uiState.cooldownSecondsRemaining
                            )
                        } else {
                            stringResource(id = R.string.verification_banner_resend)
                        }
                        Text(text = label)
                    }
                }
            }

            IconButton(onClick = onDismissClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.verification_banner_dismiss)
                )
            }
        }
    }
}

