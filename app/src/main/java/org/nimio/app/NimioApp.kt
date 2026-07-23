package org.nimio.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.nimio.app.feature.account.data.DefaultLocalProfileRepository
import org.nimio.app.feature.account.data.ProfilePreferencesDataSource
import org.nimio.app.feature.onboarding.ui.OnboardingScreen
import org.nimio.app.navigation.NimioNavHost
import org.nimio.app.ui.splash.NimioSplashScreen
import org.nimio.app.ui.theme.NimioTheme

@Composable
fun NimioApp() {
    var showSplash by remember { mutableStateOf(true) }
    var startAnimation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val profileRepository = remember(context) {
        DefaultLocalProfileRepository(ProfilePreferencesDataSource(context))
    }
    val profile by profileRepository.observeProfile().collectAsStateWithLifecycle(
        initialValue = org.nimio.app.feature.account.domain.LocalProfile()
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1700)
        showSplash = false
    }

    NimioTheme {
        if (showSplash) {
            NimioSplashScreen(startAnimation = startAnimation)
        } else if (!profile.onboardingCompleted) {
            OnboardingScreen(
                onContinue = { displayName, bio ->
                    coroutineScope.launch {
                        profileRepository.completeOnboarding(displayName, bio)
                    }
                }
            )
        } else {
            NimioNavHost(profileRepository = profileRepository)
        }
    }
}

