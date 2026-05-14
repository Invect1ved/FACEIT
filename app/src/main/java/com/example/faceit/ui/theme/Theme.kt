package com.example.faceit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FaceitDarkScheme = darkColorScheme(
    primary = FaceitOrange,
    onPrimary = Color.Black,
    primaryContainer = FaceitOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = FaceitOrangeDark,
    onSecondary = Color.White,
    tertiary = FaceitOrange,
    background = FaceitBackground,
    onBackground = Color(0xFFE8E8E8),
    surface = FaceitSurface,
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = FaceitSurfaceVariant,
    onSurfaceVariant = Color(0xFFB3B3B3),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun FACEITTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FaceitDarkScheme,
        typography = Typography,
        content = content
    )
}
