package org.nimio.app

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.nimio.app.core.network.AuthTokenDataSource
import org.nimio.app.feature.account.domain.AccountRepository
import org.nimio.app.feature.account.domain.LocalProfileRepository
import org.nimio.app.feature.account.ui.AuthScreen
import org.nimio.app.feature.social.domain.SocialGraphRepository
import org.nimio.app.feature.status.domain.StatusRepository
import org.nimio.app.navigation.NimioNavHost
import org.nimio.app.ui.splash.NimioSplashScreen
import org.nimio.app.ui.theme.NimioTheme

@Composable
fun NimioApp(
    accountRepository: AccountRepository,
    authTokenDataSource: AuthTokenDataSource,
    profileRepository: LocalProfileRepository,
    socialGraphRepository: SocialGraphRepository,
    statusRepository: StatusRepository
) {
    var showSplash by remember { mutableStateOf(true) }
    var startAnimation by remember { mutableStateOf(false) }
    var authNoticeCode by remember { mutableStateOf<String?>(null) }
    val session by accountRepository.observeSession().collectAsStateWithLifecycle(
        initialValue = null
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1700)
        showSplash = false
    }

    LaunchedEffect(session?.isSignedIn) {
        if (session?.isSignedIn == true) {
            authNoticeCode = null
            accountRepository.refreshSession()
        }
    }

    LaunchedEffect(showSplash, session?.isSignedIn) {
        if (!showSplash && session?.isSignedIn != true) {
            authNoticeCode = authTokenDataSource.consumeLogoutNotice()
        }
    }

    NimioTheme {
        val authNoticeMessage = when (authNoticeCode) {
            AuthTokenDataSource.LOGOUT_NOTICE_SESSION_EXPIRED -> stringResource(
                id = R.string.auth_logged_out_session_expired
            )
            else -> null
        }
        val stage = when {
            showSplash -> "splash"
            session?.isSignedIn != true -> "auth"
            else -> "app"
        }

        Crossfade(targetState = stage, label = "appStage") { currentStage ->
            when (currentStage) {
                "splash" -> NimioSplashScreen(startAnimation = startAnimation)
                "auth" -> AuthScreen(
                    accountRepository = accountRepository,
                    logoutNoticeMessage = authNoticeMessage
                )
                else -> NimioNavHost(
                    profileRepository = profileRepository,
                    accountRepository = accountRepository,
                    socialGraphRepository = socialGraphRepository,
                    statusRepository = statusRepository
                )
            }
        }
    }
}

