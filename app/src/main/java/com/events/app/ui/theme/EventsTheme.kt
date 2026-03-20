package com.events.app.ui.theme

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

// ─── Светлая тема ────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary              = Color(0xFF3B5BDB),
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFFDDE3FF),
    onPrimaryContainer   = Color(0xFF001257),

    secondary            = Color(0xFF585E72),
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFFDCE1F9),
    onSecondaryContainer = Color(0xFF151B2C),

    tertiary             = Color(0xFF6B4FA0),
    tertiaryContainer    = Color(0xFFEBDDFF),
    onTertiaryContainer  = Color(0xFF250058),

    error                = Color(0xFFBA1A1A),
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002),

    // Фоны — светло-серые
    background           = Color(0xFFF6F6FB),
    onBackground         = Color(0xFF1A1C24),

    // Surface чуть светлее background
    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF1A1C24),
    surfaceVariant       = Color(0xFFE4E5F0),
    onSurfaceVariant     = Color(0xFF44475A),

    outline              = Color(0xFF757888),
    outlineVariant       = Color(0xFFC4C6D8),
)

// ─── Тёмная тема ─────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFFB3C5FF),
    onPrimary            = Color(0xFF002085),
    primaryContainer     = Color(0xFF1E3AAA),
    onPrimaryContainer   = Color(0xFFDDE3FF),

    secondary            = Color(0xFFC0C5DD),
    onSecondary          = Color(0xFF293042),
    secondaryContainer   = Color(0xFF404659),
    onSecondaryContainer = Color(0xFFDCE1F9),

    tertiary             = Color(0xFFD3BBFF),
    tertiaryContainer    = Color(0xFF523787),
    onTertiaryContainer  = Color(0xFFEBDDFF),

    error                = Color(0xFFFFB4AB),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),

    // Тёмные фоны
    background           = Color(0xFF111318),
    onBackground         = Color(0xFFE3E2EA),

    surface              = Color(0xFF1C1E27),
    onSurface            = Color(0xFFE3E2EA),
    surfaceVariant       = Color(0xFF3A3C4A),
    onSurfaceVariant     = Color(0xFFC5C6D9),

    outline              = Color(0xFF8E8FA3),
    outlineVariant       = Color(0xFF44475A),
)

@Composable
fun EventsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Статус-бар в цвет background темы
            window.statusBarColor = colorScheme.background.toArgb()
            // Иконки статус-бара: тёмные на светлом, светлые на тёмном
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content     = content
    )
}