package org.nimio.app.feature.account.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nimio.app.R
import org.nimio.app.core.ui.PlaceholderScreenCard

@Composable
fun AccountScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PlaceholderScreenCard(
            title = stringResource(id = R.string.account_screen_title),
            description = stringResource(id = R.string.account_screen_description)
        )
        PlaceholderScreenCard(
            title = stringResource(id = R.string.account_identity_title),
            description = stringResource(id = R.string.account_identity_description)
        )
        PlaceholderScreenCard(
            title = stringResource(id = R.string.account_auth_title),
            description = stringResource(id = R.string.account_auth_description)
        )
        androidx.compose.material3.Text(
            text = stringResource(id = R.string.sync_status_pending),
            style = MaterialTheme.typography.labelLarge
        )
    }
}



