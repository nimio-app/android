package org.nimio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NimioCrimson,
    onPrimary = Color.White,
    primaryFixed = NimioCoral,
    primaryContainer = NimioCrimsonContainerLight,
    onPrimaryContainer = NimioRose,
    secondary = NimioCoral,
    onSecondary = Color.White,
    background = BackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    onBackground = OnSurfaceLight,
    outlineVariant = OutlineSoft
)

@Composable
fun NimioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}