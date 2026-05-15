package com.example.ajeschat.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AjesGreen,
    onPrimary = AjesOnGreen,
    primaryContainer = AjesBorder,
    onPrimaryContainer = AjesTextPrimary,
    secondary = AjesMutedGreen,
    onSecondary = AjesOnGreen,
    secondaryContainer = AjesMintBg2,
    onSecondaryContainer = AjesTextPrimary,
    tertiary = AjesGreenLight,
    onTertiary = AjesTextPrimary,
    tertiaryContainer = AjesSage,
    onTertiaryContainer = AjesTextPrimary,
    background = AjesMintBg,
    onBackground = AjesTextPrimary,
    surface = Color.White,
    onSurface = AjesTextPrimary,
    surfaceVariant = AjesMintBg2,
    onSurfaceVariant = AjesTextSecondary,
    outline = AjesBorder,
    outlineVariant = AjesSage,
    error = AjesError,
    onError = AjesOnGreen,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = AjesError
)

private val DarkColorScheme = darkColorScheme(
    primary = AjesGreenLight,
    onPrimary = AjesGreenDark,
    primaryContainer = AjesGreenDark,
    onPrimaryContainer = AjesBorder,
    secondary = AjesSage,
    onSecondary = AjesGreenDark,
    secondaryContainer = Color(0xFF2E4A30),
    onSecondaryContainer = AjesBorder,
    tertiary = AjesGreenAccent,
    onTertiary = AjesGreenDark,
    tertiaryContainer = Color(0xFF3D5C40),
    onTertiaryContainer = AjesBorder,
    background = Color(0xFF121A12),
    onBackground = Color(0xFFE8F5E9),
    surface = Color(0xFF1E2E1E),
    onSurface = Color(0xFFE8F5E9),
    surfaceVariant = Color(0xFF2A3D2A),
    onSurfaceVariant = Color(0xFFC8E6C9),
    outline = AjesMutedGreen,
    outlineVariant = Color(0xFF3D5C40),
    error = Color(0xFFEF9A9A),
    onError = AjesGreenDark,
    errorContainer = Color(0xFF4A1F1F),
    onErrorContainer = Color(0xFFFFCDD2)
)

@Composable
fun AJESCHATTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
