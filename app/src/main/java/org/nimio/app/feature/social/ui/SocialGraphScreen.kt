package org.nimio.app.feature.social.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nimio.app.R
import org.nimio.app.core.ui.PlaceholderScreenCard

@Composable
fun SocialGraphScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PlaceholderScreenCard(
            title = stringResource(id = R.string.social_screen_title),
            description = stringResource(id = R.string.social_screen_description)
        )
        PlaceholderScreenCard(
            title = stringResource(id = R.string.social_circles_title),
            description = stringResource(id = R.string.social_circles_description)
        )
        PlaceholderScreenCard(
            title = stringResource(id = R.string.social_requests_title),
            description = stringResource(id = R.string.social_requests_description)
        )
        Text(
            text = stringResource(id = R.string.sync_status_pending),
            style = MaterialTheme.typography.labelLarge
        )
    }
}



