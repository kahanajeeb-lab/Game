package com.example.ui.theme

import android.app.Activity
import android.os.Build
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
    primary = WhatsAppGreen,
    onPrimary = Color(0xFF0B141A),
    primaryContainer = WhatsAppDarkOutgoing,
    onPrimaryContainer = Color(0xFFE9EDEF),
    secondary = WhatsAppLightGreen,
    onSecondary = Color(0xFF0B141A),
    background = WhatsAppDarkBg,
    onBackground = WhatsAppTextPrimary,
    surface = WhatsAppDarkSurface,
    onSurface = WhatsAppTextPrimary,
    surfaceVariant = WhatsAppDarkIncoming,
    onSurfaceVariant = WhatsAppTextSecondary,
    outline = WhatsAppDivider
)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7FFDB),
    onPrimaryContainer = Color(0xFF00382E),
    secondary = WhatsAppGreen,
    onSecondary = Color.White,
    background = WhatsAppLightBg,
    onBackground = Color(0xFF111B21),
    surface = Color.White,
    onSurface = Color(0xFF111B21),
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF667781),
    outline = Color(0xFFE9EDEF)
)

@Composable
fun SecureChatTheme(
    darkTheme: Boolean = true, // Default to sleek WhatsApp Dark
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) WhatsAppDarkSurface.toArgb() else WhatsAppTeal.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
