package org.nimio.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import org.nimio.app.navigation.NimioNavHost
import org.nimio.app.ui.splash.NimioSplashScreen
import org.nimio.app.ui.theme.NimioTheme

@Composable
fun NimioApp() {
    var showSplash by remember { mutableStateOf(true) }
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1700)
        showSplash = false
    }

    NimioTheme {
        if (showSplash) {
            NimioSplashScreen(startAnimation = startAnimation)
        } else {
            NimioNavHost()
        }
    }
}

