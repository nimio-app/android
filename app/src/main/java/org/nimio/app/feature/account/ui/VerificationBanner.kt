package org.nimio.app.feature.account.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.nimio.app.R

private val VerificationBannerContainer = Color(0xFFFFF8E1)
private val VerificationBannerContent = Color(0xFF4E342E)
private val VerificationBannerButton = Color(0xFFF9A825)

@Composable
fun VerificationBanner(
    uiState: EmailVerificationBannerUiState,
    onResendClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!uiState.shouldShow) return

    val resendEnabled = !uiState.isResending && uiState.cooldownSecondsRemaining == 0
    val resendLabel = when {
        uiState.isResending -> stringResource(id = R.string.verification_banner_sending)
        uiState.cooldownSecondsRemaining > 0 -> stringResource(
            id = R.string.verification_banner_resend_cooldown,
            uiState.cooldownSecondsRemaining
        )
        else -> stringResource(id = R.string.verification_banner_resend)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = VerificationBannerContainer,
            contentColor = VerificationBannerContent
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(20.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.verification_banner_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(
                            id = R.string.verification_banner_subtitle,
                            uiState.email
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                IconButton(onClick = onDismissClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.verification_banner_dismiss)
                    )
                }
            }

            Button(
                onClick = onResendClick,
                enabled = resendEnabled,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerificationBannerButton,
                    contentColor = VerificationBannerContent
                )
            ) {
                if (uiState.isResending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = VerificationBannerContent
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = resendLabel,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

