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

data object ConnectionsDestination : NimioDestination {
    override val route: String = "connections"
    override val labelResId: Int = R.string.nav_connections
    override val icon: ImageVector = Icons.Outlined.Groups
}

data object ProfileDestination : NimioDestination {
    override val route: String = "profile"
    override val labelResId: Int = R.string.nav_profile
    override val icon: ImageVector = Icons.Outlined.AccountCircle
}

val topLevelDestinations: List<NimioDestination> = listOf(
    StatusDestination,
    ConnectionsDestination,
    ProfileDestination
)


