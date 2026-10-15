package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NavioColors = darkColorScheme(
    primary = Teal,
    onPrimary = Background,
    primaryContainer = TealContainer,
    onPrimaryContainer = TextPrimary,
    secondary = TextSecondary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = Surface,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = SurfaceHighest,
    surfaceContainerLow = Surface,
    outline = Outline,
    outlineVariant = Outline,
    error = ErrorRed
)

@Composable
fun NavioTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NavioColors, typography = Typography(), content = content)
}
