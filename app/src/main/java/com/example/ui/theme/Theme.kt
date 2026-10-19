package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = Color(0xFF001F26),
    primaryContainer = Color(0xFF003640),
    onPrimaryContainer = Color(0xFF70F5FF),

    secondary = CyberSecondary,
    onSecondary = Color(0xFF00210A),
    secondaryContainer = Color(0xFF003914),
    onSecondaryContainer = Color(0xFF73FF91),

    tertiary = CyberTertiary,
    onTertiary = Color(0xFF261A00),
    tertiaryContainer = Color(0xFF402E00),
    onTertiaryContainer = Color(0xFFFFD470),

    error = CyberError,
    onError = Color(0xFF330010),
    errorContainer = Color(0xFF59001D),
    onErrorContainer = Color(0xFFFF85A1),

    background = CyberBackground,
    onBackground = CyberTextPrimary,

    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberTextSecondary,

    outline = CyberBorder,
    outlineVariant = CyberSurfaceElevated
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Cyberpunk theme is intentionally dark
    dynamicColor: Boolean = false, // Keep the custom Raunak Exploits neon palette
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
