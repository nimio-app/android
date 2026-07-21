package org.nimio.app.feature.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.nimio.app.R
import org.nimio.app.core.ui.PlaceholderScreen

@Composable
fun ProfileScreen() {
    PlaceholderScreen(
        title = stringResource(id = R.string.profile_screen_title),
        description = stringResource(id = R.string.profile_screen_description)
    )
}

