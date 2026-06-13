package com.istech.expensestracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = White,
    primaryContainer = LightPrimary,
    onPrimaryContainer = FontPrimary,
    secondary = DarkGreen,
    onSecondary = White,
    secondaryContainer = LowGreen,
    onSecondaryContainer = DarkGreen,
    tertiary = DarkRed,
    onTertiary = White,
    tertiaryContainer = LightRed,
    onTertiaryContainer = DarkRed,
    background = Background,
    onBackground = FontPrimary,
    surface = White,
    onSurface = FontPrimary,
    error = DarkRed,
    onError = White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = White,
    primaryContainer = FontPrimary,
    onPrimaryContainer = LightPrimary,
    secondary = LowGreen,
    onSecondary = DarkGreen,
    secondaryContainer = DarkGreen,
    onSecondaryContainer = LowGreen,
    tertiary = LightRed,
    onTertiary = DarkRed,
    tertiaryContainer = DarkRed,
    onTertiaryContainer = LightRed,
    background = FontPrimary,
    onBackground = Background,
    surface = FontPrimary,
    onSurface = Background,
    error = LightRed,
    onError = DarkRed
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
