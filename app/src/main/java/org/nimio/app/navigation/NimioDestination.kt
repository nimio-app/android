package org.nimio.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.ui.graphics.vector.ImageVector
import org.nimio.app.R

sealed interface NimioDestination {
    val route: String
    @get:StringRes
    val labelResId: Int
    val icon: ImageVector
}

data object StatusDestination : NimioDestination {
    override val route: String = "status"
    override val labelResId: Int = R.string.nav_status
    override val icon: ImageVector = Icons.Outlined.Schedule
}

data object SocialDestination : NimioDestination {
    override val route: String = "social"
    override val labelResId: Int = R.string.nav_social
    override val icon: ImageVector = Icons.Outlined.Groups
}

data object AccountDestination : NimioDestination {
    override val route: String = "account"
    override val labelResId: Int = R.string.nav_account
    override val icon: ImageVector = Icons.Outlined.AccountCircle
}

val topLevelDestinations: List<NimioDestination> = listOf(
    StatusDestination,
    SocialDestination,
    AccountDestination
)


