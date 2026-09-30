package com.rising.pos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Pure OLED Black palette for dark theme
val PureBlack = Color(0xFF000000)
val DarkSurface = Color(0xFF121212)
val DarkSurfaceVariant = Color(0xFF1E1E1E)
val DarkOutline = Color(0xFF27272A)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueContainer,
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = SuccessGreen,
    onSecondary = Color.White,
    secondaryContainer = SuccessGreenContainer,
    onSecondaryContainer = Color(0xFF0F172A),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFE2E8F0),
    error = DangerRed,
    errorContainer = DangerRedContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFFF8FAFC),
    secondary = SuccessGreenLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF14532D),
    onSecondaryContainer = Color(0xFFF0FDF4),
    background = PureBlack,
    onBackground = Color(0xFFF8FAFC),
    surface = DarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFE4E4E7),
    outline = DarkOutline,
    error = DangerRed,
    errorContainer = DangerRedContainer
)

data class PosThemeColors(
    val slate900: Color,
    val slate800: Color,
    val slate700: Color,
    val slate600: Color,
    val slate500: Color,
    val slate400: Color,
    val slate200: Color,
    val slate100: Color,
    val slate50: Color,
    val isDark: Boolean
)

val LocalPosColors = staticCompositionLocalOf {
    PosThemeColors(
        slate900 = Color(0xFF0F172A),
        slate800 = Color(0xFF1E293B),
        slate700 = Color(0xFF334155),
        slate600 = Color(0xFF475569),
        slate500 = Color(0xFF64748B),
        slate400 = Color(0xFF94A3B8),
        slate200 = Color(0xFFE2E8F0),
        slate100 = Color(0xFFF1F5F9),
        slate50 = Color(0xFFF8FAFC),
        isDark = false
    )
}

@Composable
fun RisingPosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val posColors = if (darkTheme) {
        PosThemeColors(
            slate900 = Color(0xFFFFFFFF),
            slate800 = Color(0xFFF4F4F5),
            slate700 = Color(0xFFE4E4E7),
            slate600 = Color(0xFFD4D4D8),
            slate500 = Color(0xFFA1A1AA),
            slate400 = Color(0xFF71717A),
            slate200 = Color(0xFF27272A),
            slate100 = Color(0xFF18181B),
            slate50 = PureBlack,
            isDark = true
        )
    } else {
        PosThemeColors(
            slate900 = Color(0xFF0F172A),
            slate800 = Color(0xFF1E293B),
            slate700 = Color(0xFF334155),
            slate600 = Color(0xFF475569),
            slate500 = Color(0xFF64748B),
            slate400 = Color(0xFF94A3B8),
            slate200 = Color(0xFFE2E8F0),
            slate100 = Color(0xFFF1F5F9),
            slate50 = Color(0xFFF8FAFC),
            isDark = false
        )
    }

    CompositionLocalProvider(LocalPosColors provides posColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
