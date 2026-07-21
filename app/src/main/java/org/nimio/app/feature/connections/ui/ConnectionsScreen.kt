package org.nimio.app.feature.connections.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.nimio.app.R
import org.nimio.app.core.ui.PlaceholderScreen

@Composable
fun ConnectionsScreen() {
    PlaceholderScreen(
        title = stringResource(id = R.string.connections_screen_title),
        description = stringResource(id = R.string.connections_screen_description)
    )
}

