package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = Yellow,
    secondary = Blue2,
    tertiary = Blue1,
    background = SlateDarkBackground,
    surface = SlateDarkSurface,
    onPrimary = Color(0xFF0E1325),
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE5EDF6),
    onSurface = Color(0xFFE5EDF6),
    surfaceVariant = SlateDarkCard,
    onSurfaceVariant = Color(0xFFE5EDF6)
)

private val LightColorScheme = lightColorScheme(
    primary = Blue1,
    secondary = Blue2,
    tertiary = Yellow,
    background = SlateLightBackground,
    surface = SlateLightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF0E1325),
    onBackground = Color(0xFF0E1325),
    onSurface = Color(0xFF0E1325),
    surfaceVariant = SlateLightCard,
    onSurfaceVariant = Color(0xFF0E1325)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
