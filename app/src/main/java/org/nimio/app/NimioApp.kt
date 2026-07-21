package org.nimio.app

import androidx.compose.runtime.Composable
import org.nimio.app.navigation.NimioNavHost
import org.nimio.app.ui.theme.NimioTheme

@Composable
fun NimioApp() {
    NimioTheme {
        NimioNavHost()
    }
}

