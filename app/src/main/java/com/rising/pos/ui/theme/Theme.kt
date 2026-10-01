package com.rising.pos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onPrimaryContainer = Color(0xFF182230),
    secondary = SuccessGreen,
    onSecondary = Color.White,
    secondaryContainer = SuccessGreenContainer,
    onSecondaryContainer = Color(0xFF182230),
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF182230),
    surface = Color.White,
    onSurface = Color(0xFF182230),
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF334155),
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F8FA),
    surfaceContainer = Color(0xFFF0F2F5),
    surfaceContainerHigh = Color(0xFFE9EDF2),
    surfaceContainerHighest = Color(0xFFE2E7ED),
    outlineVariant = Color(0xFFE2E8F0),
    outline = Color(0xFFE2E8F0),
    error = DangerRed,
    errorContainer = DangerRedContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA6C8FF),
    onPrimary = Color(0xFF102D57),
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFFF7F8FA),
    secondary = SuccessGreenLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF14532D),
    onSecondaryContainer = Color(0xFFF0FDF4),
    background = PureBlack,
    onBackground = Color(0xFFF7F8FA),
    surface = DarkSurface,
    onSurface = Color(0xFFF7F8FA),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFE4E4E7),
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = PureBlack,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceVariant,
    surfaceContainerHigh = Color(0xFF25272B),
    surfaceContainerHighest = Color(0xFF303338),
    outlineVariant = DarkOutline,
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
        slate900 = Color(0xFF182230),
        slate800 = Color(0xFF1E293B),
        slate700 = Color(0xFF334155),
        slate600 = Color(0xFF475569),
        slate500 = Color(0xFF64748B),
        slate400 = Color(0xFF727F90),
        slate200 = Color(0xFFE2E8F0),
        slate100 = Color(0xFFF0F2F5),
        slate50 = Color(0xFFF7F8FA),
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
            slate900 = Color(0xFF182230),
            slate800 = Color(0xFF1E293B),
            slate700 = Color(0xFF334155),
            slate600 = Color(0xFF475569),
            slate500 = Color(0xFF64748B),
            slate400 = Color(0xFF727F90),
            slate200 = Color(0xFFE2E8F0),
            slate100 = Color(0xFFF0F2F5),
            slate50 = Color(0xFFF7F8FA),
            isDark = false
        )
    }

    CompositionLocalProvider(LocalPosColors provides posColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = Shapes(
                extraSmall = RoundedCornerShape(6.dp),
                small = RoundedCornerShape(10.dp),
                medium = RoundedCornerShape(12.dp),
                large = RoundedCornerShape(16.dp),
                extraLarge = RoundedCornerShape(24.dp)
            ),
            typography = PosTypography,
            content = content
        )
    }
}

private val PosTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.8).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.6).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 30.sp, letterSpacing = (-0.4).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp)
)
