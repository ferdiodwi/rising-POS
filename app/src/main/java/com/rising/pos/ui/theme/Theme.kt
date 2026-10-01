package com.rising.pos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tema Rising POS. Sumber kebenaran arah desain ada di DESIGN.md.
 *
 * Prinsip: netral gelap untuk keterbacaan, satu aksen navy, status sebagai
 * informasi. Tidak ada gradient, glow, atau glassmorphism. Shadow hanya untuk
 * elemen yang benar-benar melayang (bottom sheet, bar keranjang).
 */

// --- Netral terang ---
private val Ink = Color(0xFF0F172A)
private val Ink2 = Color(0xFF475569)
private val Muted = Color(0xFF5B6879)
private val Bg = Color(0xFFF6F7F9)
private val SurfaceLight = Color(0xFFFFFFFF)
private val SurfaceVariantLight = Color(0xFFF0F2F5)
private val Line = Color(0xFFE4E7EC)
private val LineStrongLight = Color(0xFF828C99)

// --- Netral gelap (mode gelap tetap dijaga, kontras diverifikasi) ---
private val DarkBg = Color(0xFF0B0E14)
private val DarkSurface = Color(0xFF14181F)
private val DarkSurfaceVariant = Color(0xFF1C222B)
private val DarkInk = Color(0xFFF1F5F9)
private val DarkInk2 = Color(0xFFC2CAD6)
private val DarkMuted = Color(0xFF9AA6B5)
private val DarkLine = Color(0xFF2A323D)
private val DarkLineStrong = Color(0xFF6B7686)

// --- Aksen navy (terang & gelap) ---
private val Navy = Color(0xFF1E40AF)
private val NavyDeep = Color(0xFF16307A)
private val NavySubtle = Color(0xFFEEF3FF)
private val NavyDark = Color(0xFF93B4FF)      // aksen pada mode gelap
private val NavyDarkContainer = Color(0xFF1B2A4A)

private val LightColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = NavySubtle,
    onPrimaryContainer = Color(0xFF13265C),
    secondary = SuccessGreen,
    onSecondary = Color.White,
    secondaryContainer = SuccessGreenContainer,
    onSecondaryContainer = Color(0xFF06341F),
    background = Bg,
    onBackground = Ink,
    surface = SurfaceLight,
    onSurface = Ink,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Ink2,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = SurfaceLight,
    surfaceContainerLow = Bg,
    surfaceContainer = SurfaceVariantLight,
    surfaceContainerHigh = Color(0xFFE9EDF2),
    surfaceContainerHighest = Color(0xFFE2E7ED),
    outline = LineStrongLight,
    outlineVariant = Line,
    error = DangerRed,
    onError = Color.White,
    errorContainer = DangerRedContainer,
    onErrorContainer = Color(0xFF5A0F0F)
)

private val DarkColorScheme = darkColorScheme(
    primary = NavyDark,
    onPrimary = Color(0xFF0B1B3A),
    primaryContainer = NavyDarkContainer,
    onPrimaryContainer = Color(0xFFDCE7FF),
    secondary = SuccessGreenLight,
    onSecondary = Color(0xFF04291A),
    secondaryContainer = Color(0xFF0E3A2A),
    onSecondaryContainer = Color(0xFFCFF3E2),
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkInk2,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = DarkBg,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceVariant,
    surfaceContainerHigh = Color(0xFF222A34),
    surfaceContainerHighest = Color(0xFF2A323D),
    outline = DarkLineStrong,
    outlineVariant = DarkLine,
    error = Color(0xFFFF8A8A),
    onError = Color(0xFF3A0A0A),
    errorContainer = Color(0xFF5A1414),
    onErrorContainer = Color(0xFFFFD9D9)
)

/**
 * Token netral yang tidak punya slot di ColorScheme Material.
 * Dipetakan ke LocalPosColors agar konsisten lintas layar.
 */
data class PosThemeColors(
    val slate900: Color,   // teks utama / angka uang
    val slate800: Color,
    val slate700: Color,   // teks sekunder kuat
    val slate600: Color,
    val slate500: Color,   // teks tersier / caption
    val slate400: Color,   // ikon nonaktif
    val slate200: Color,   // garis dekoratif
    val slate100: Color,
    val slate50: Color,    // latar layar
    val lineStrong: Color, // garis informatif (outline input/fokus)
    val isDark: Boolean
)

private val LightPosColors = PosThemeColors(
    slate900 = Ink,
    slate800 = Color(0xFF1E293B),
    slate700 = Color(0xFF334155),
    slate600 = Ink2,
    slate500 = Muted,
    slate400 = Color(0xFF7A8492),
    slate200 = Line,
    slate100 = SurfaceVariantLight,
    slate50 = Bg,
    lineStrong = LineStrongLight,
    isDark = false
)

private val DarkPosColors = PosThemeColors(
    slate900 = DarkInk,
    slate800 = Color(0xFFE2E8F0),
    slate700 = Color(0xFFCBD5E1),
    slate600 = DarkInk2,
    slate500 = DarkMuted,
    slate400 = Color(0xFF7C8798),
    slate200 = DarkLine,
    slate100 = DarkSurfaceVariant,
    slate50 = DarkBg,
    lineStrong = DarkLineStrong,
    isDark = true
)

val LocalPosColors = staticCompositionLocalOf { LightPosColors }

/**
 * Figur tabular: semua digit selebar sama supaya nominal uang sejajar vertikal
 * (motif "bahasa struk" pada DESIGN.md). Nilai "tnum" = tabular numbers.
 */
private const val TabularFigures = "tnum"

/** Style khusus angka uang. Dipakai lewat [PosTextStyles]. */
object PosTextStyles {
    /** Total bayar / omzet hero. */
    val displayMoney = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = TabularFigures
    )

    /** Nominal pada baris ringkasan. */
    val money = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFeatureSettings = TabularFigures
    )

    /** Harga pada kartu produk. */
    val priceCard = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontFeatureSettings = TabularFigures
    )

    /** Nomor struk: monospace karena dipindai karakter-per-karakter. */
    val receiptNo = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}

private val PosTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.3).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp)
)

private val PosShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun RisingPosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val posColors = if (darkTheme) DarkPosColors else LightPosColors

    CompositionLocalProvider(LocalPosColors provides posColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = PosShapes,
            typography = PosTypography,
            content = content
        )
    }
}
