package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
    primary = VeyraWhite,
    onPrimary = VeyraBlack,
    primaryContainer = VeyraCardElevated,
    onPrimaryContainer = VeyraWhite,
    secondary = VeyraTextSecondary,
    onSecondary = VeyraWhite,
    background = VeyraBlack,
    onBackground = VeyraTextPrimary,
    surface = VeyraCard,
    onSurface = VeyraTextPrimary,
    surfaceVariant = VeyraCardElevated,
    onSurfaceVariant = VeyraTextSecondary,
    outline = VeyraBorder,
    outlineVariant = VeyraBorderSubtle,
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}

