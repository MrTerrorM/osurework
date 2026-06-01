package com.example.osurework.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OsuColorScheme = darkColorScheme(
    primary          = Accent,
    onPrimary        = OnAccent,
    primaryContainer = AccentLight,
    background       = Background,
    onBackground     = OnBackground,
    surface          = Surface,
    onSurface        = OnSurface,
    surfaceVariant   = Color(0xFF332840),
    onSurfaceVariant = OnSurface,
    outline          = Subtle
)

@Composable
fun OsuReworkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OsuColorScheme,
        typography  = Typography,
        content     = content
    )
}