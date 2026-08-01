package org.nimio.app.core.common

import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult()
    data class Error(val exception: Exception) : GoogleSignInResult()
    data object Cancelled : GoogleSignInResult()
}

fun signInWithGoogle(
    activity: ComponentActivity,
    webClientId: String,
    onResult: (GoogleSignInResult) -> Unit
) {
    Timber.d("Starting Google sign-in flow")
    val credentialManager = CredentialManager.create(activity)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(webClientId)
        .setFilterByAuthorizedAccounts(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    activity.lifecycleScope.launch {
        try {
            val result = credentialManager.getCredential(
                context = activity,
                request = request
            )
            Timber.d("Google credential received: %s", result.credential.type)
            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            Timber.d("Google ID token extracted successfully (len=%d)", idToken.length)
            onResult(GoogleSignInResult.Success(idToken))
        } catch (e: GetCredentialCancellationException) {
            Timber.d("Google sign-in cancelled by user")
            onResult(GoogleSignInResult.Cancelled)
        } catch (e: GetCredentialException) {
            Timber.w(e, "Google sign-in credential flow failed")
            onResult(GoogleSignInResult.Error(e))
        } catch (e: Exception) {
            Timber.e(e, "Unexpected Google sign-in failure")
            onResult(GoogleSignInResult.Error(e))
        }
    }
}



