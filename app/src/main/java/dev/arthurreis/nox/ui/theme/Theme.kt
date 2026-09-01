package dev.arthurreis.nox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NoxColorScheme = darkColorScheme(
    primary = NoxRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF49000E),
    onPrimaryContainer = Color(0xFFFFD9DF),
    secondary = NoxGreen,
    onSecondary = Color(0xFF071B0B),
    background = NoxBackground,
    onBackground = NoxText,
    surface = NoxSurface,
    onSurface = NoxText,
    surfaceVariant = NoxSurfaceRaised,
    onSurfaceVariant = NoxTextMuted,
    outline = NoxBorder,
    error = Color(0xFFFF7B72),
)

@Composable
fun NoxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NoxColorScheme,
        typography = NoxTypography,
        content = content,
    )
}
