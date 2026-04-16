package com.events.app.ui.theme

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

// ─── Светлая тема ────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary              = Color(0xFF1F3FCC),
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFFCFD8FF),
    onPrimaryContainer   = Color(0xFF001257),

    secondary            = Color(0xFF4A5270),
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFFD4DAF8),
    onSecondaryContainer = Color(0xFF151B2C),

    tertiary             = Color(0xFF5C3FA0),
    tertiaryContainer    = Color(0xFFE5D5FF),
    onTertiaryContainer  = Color(0xFF250058),

    error                = Color(0xFFBA1A1A),
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002),

    background           = Color(0xFFF6F6FB),
    onBackground         = Color(0xFF1A1C24),

    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF1A1C24),
    surfaceVariant       = Color(0xFFE4E5F0),
    onSurfaceVariant     = Color(0xFF44475A),

    outline              = Color(0xFF757888),
    outlineVariant       = Color(0xFFC4C6D8),
)

// ─── Тёмная тема ─────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFF96AEFF),
    onPrimary            = Color(0xFF001A78),
    primaryContainer     = Color(0xFF1E3DB8),
    onPrimaryContainer   = Color(0xFFDDE3FF),

    secondary            = Color(0xFFACB5D8),
    onSecondary          = Color(0xFF293042),
    secondaryContainer   = Color(0xFF404659),
    onSecondaryContainer = Color(0xFFDCE1F9),

    tertiary             = Color(0xFFC1A5FF),
    tertiaryContainer    = Color(0xFF523787),
    onTertiaryContainer  = Color(0xFFEBDDFF),

    error                = Color(0xFFFFB4AB),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),

    // Фон — заметно темнее карточек
    background           = Color(0xFF0E1015),  // было 0xFF111318
    onBackground         = Color(0xFFE3E2EA),

    // Карточки — заметно светлее фона
    surface              = Color(0xFF252830),  // было 0xFF1C1E27
    onSurface            = Color(0xFFE3E2EA),
    surfaceVariant       = Color(0xFF3A3C4A),
    onSurfaceVariant     = Color(0xFFC5C6D9),

    outline              = Color(0xFF8E8FA3),
    outlineVariant       = Color(0xFF55576A),
)

@Composable
fun EventsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context).copy(
                background = DarkColorScheme.background,
                surface    = DarkColorScheme.surface,
            )
            else dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}