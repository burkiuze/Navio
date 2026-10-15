package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NavioDarkColorScheme = darkColorScheme(
    primary = NavioCyan,
    onPrimary = BackgroundDark,
    primaryContainer = NavioCyanContainer,
    onPrimaryContainer = NavioCyanOnContainer,
    secondary = NavioViolet,
    onSecondary = BackgroundDark,
    secondaryContainer = NavioVioletContainer,
    onSecondaryContainer = NavioVioletOnContainer,
    tertiary = NavioEmerald,
    onTertiary = BackgroundDark,
    tertiaryContainer = NavioEmeraldContainer,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceContainerDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SurfaceBorderDark,
    outlineVariant = SurfaceContainerHighDark,
    error = NavioRose
)

@Composable
fun NavioTheme(
    darkTheme: Boolean = true, // Dark theme by default per specification
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NavioDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    NavioTheme(darkTheme = darkTheme, content = content)
}
