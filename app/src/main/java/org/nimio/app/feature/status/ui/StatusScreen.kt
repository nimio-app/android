package org.nimio.app.feature.status.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.nimio.app.R
import org.nimio.app.core.ui.PlaceholderScreen

@Composable
fun StatusScreen() {
    PlaceholderScreen(
        title = stringResource(id = R.string.status_screen_title),
        description = stringResource(id = R.string.status_screen_description)
    )
}

