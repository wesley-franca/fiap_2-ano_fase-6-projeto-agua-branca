package com.aguiabranca.inovacao.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val primaryBlue = Color(0xFF4C63DD)
private val primaryLight = Color(0xFFE8F1FF)
private val primaryDark = Color(0xFF3D4FBF)
private val white = Color.White
private val black = Color.Black

private val LightColorScheme = lightColorScheme(
    primary = primaryBlue,
    onPrimary = white,
    primaryContainer = primaryLight,
    onPrimaryContainer = primaryDark,
    secondary = Color(0xFF757575),
    onSecondary = white,
    background = white,
    onBackground = black,
    surface = white,
    onSurface = black,
    outline = Color(0xFF9E9E9E)
)

private val DarkColorScheme = darkColorScheme(
    primary = primaryBlue,
    onPrimary = white,
    primaryContainer = primaryDark,
    onPrimaryContainer = primaryLight,
    secondary = Color(0xFF9E9E9E),
    onSecondary = black,
    outline = Color(0xFF757575)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
