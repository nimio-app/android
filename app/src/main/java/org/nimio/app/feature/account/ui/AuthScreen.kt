package org.nimio.app.feature.account.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.nimio.app.R
import org.nimio.app.core.common.GoogleSignInResult
import org.nimio.app.core.common.signInWithGoogle
import org.nimio.app.feature.account.domain.AccountRepository

@Composable
fun AuthScreen(
    accountRepository: AccountRepository
) {
    val context = LocalContext.current
    val factory = remember(accountRepository) {
        AuthViewModelFactory(accountRepository)
    }
    val viewModel: AuthViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuthBranding()

            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (uiState.isLoginMode) {
                            stringResource(id = R.string.auth_login_title)
                        } else {
                            stringResource(id = R.string.auth_register_title)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.isLoginMode) {
                            stringResource(id = R.string.auth_login_subtitle)
                        } else {
                            stringResource(id = R.string.auth_register_subtitle)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AuthModeButton(
                            text = stringResource(id = R.string.auth_create_account),
                            selected = !uiState.isLoginMode,
                            onClick = { viewModel.onModeChanged(false) },
                            modifier = Modifier.weight(1f)
                        )
                        AuthModeButton(
                            text = stringResource(id = R.string.auth_sign_in),
                            selected = uiState.isLoginMode,
                            onClick = { viewModel.onModeChanged(true) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (!uiState.isLoginMode) {
                        OutlinedTextField(
                            value = uiState.displayName,
                            onValueChange = viewModel::onDisplayNameChanged,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            label = { Text(text = stringResource(id = R.string.account_display_name_label)) },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )

                        OutlinedTextField(
                            value = uiState.username,
                            onValueChange = viewModel::onUsernameChanged,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            label = { Text(text = stringResource(id = R.string.auth_username_label)) },
                            placeholder = { Text(text = stringResource(id = R.string.auth_username_placeholder)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                        )
                    }

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        label = { Text(text = stringResource(id = R.string.auth_email_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        label = { Text(text = stringResource(id = R.string.auth_password_label)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    if (!uiState.errorMessage.isNullOrBlank()) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = viewModel::submit,
                        enabled = uiState.canSubmit && !uiState.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (uiState.isSubmitting) {
                                stringResource(id = R.string.auth_working)
                            } else if (uiState.isLoginMode) {
                                stringResource(id = R.string.auth_sign_in)
                            } else {
                                stringResource(id = R.string.auth_create_account)
                            }
                        )
                    }

                    GoogleSignInButton(
                        enabled = !uiState.isGoogleSigningIn && !uiState.isSubmitting,
                        isLoading = uiState.isGoogleSigningIn,
                        onClick = {
                            val webClientId = context.getString(R.string.google_web_client_id)
                            val activity = context as? androidx.activity.ComponentActivity
                            if (activity == null) {
                                viewModel.onGoogleSignInError("Google sign-in is unavailable in this screen.")
                                return@GoogleSignInButton
                            }

                            viewModel.onGoogleSignInStarted()
                            signInWithGoogle(activity, webClientId) { result ->
                                when (result) {
                                    is GoogleSignInResult.Success -> viewModel.googleSignIn(result.idToken)
                                    is GoogleSignInResult.Cancelled -> viewModel.onGoogleSignInCancelled()
                                    is GoogleSignInResult.Error -> viewModel.onGoogleSignInError(
                                        result.exception.message ?: "Google sign-in failed."
                                    )
                                }
                            }
                        }
                    )

                }
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = stringResource(id = R.string.auth_google_signing_in))
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_google_g),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(id = R.string.auth_google_sign_in),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
    }
}

@Composable
private fun AuthModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = text)
        }
    }
}

@Composable
private fun AuthBranding() {
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

