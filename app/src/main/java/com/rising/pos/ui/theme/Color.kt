package com.rising.pos.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Token warna Rising POS (lihat DESIGN.md).
 *
 * Nama token (Slate*) dipertahankan agar tidak memecah 29 berkas pemakai, tetapi
 * NILAINYA diperbarui mengikuti design system: netral lebih gelap agar lolos
 * kontras WCAG AA, brand navy dipertajam, dan ditambah token garis informatif.
 *
 * Semua pasangan teks/latar pada DESIGN.md sudah dihitung, bukan dikira-kira.
 */

// --- Netral (teks & permukaan) ---
val Slate900: Color
    @Composable
    get() = LocalPosColors.current.slate900

val Slate800: Color
    @Composable
    get() = LocalPosColors.current.slate800

val Slate700: Color
    @Composable
    get() = LocalPosColors.current.slate700

val Slate600: Color
    @Composable
    get() = LocalPosColors.current.slate600

val Slate500: Color
    @Composable
    get() = LocalPosColors.current.slate500

val Slate400: Color
    @Composable
    get() = LocalPosColors.current.slate400

val Slate200: Color
    @Composable
    get() = LocalPosColors.current.slate200

val Slate100: Color
    @Composable
    get() = LocalPosColors.current.slate100

val Slate50: Color
    @Composable
    get() = LocalPosColors.current.slate50

/** Garis informatif (outline input, fokus). Lolos 3:1 terhadap surface. */
val LineStrong: Color
    @Composable
    get() = LocalPosColors.current.lineStrong

// --- Brand (aksen tunggal) ---
val PrimaryBlue = Color(0xFF1E40AF)          // navy: CTA utama, state terpilih, fokus
val PrimaryBlueDeep = Color(0xFF16307A)      // navy pressed/dark
val PrimaryBlueLight = Color(0xFF3B82F6)
val PrimaryBlueContainer = Color(0xFFEEF3FF) // navySubtle: latar tint chip terpilih/badge

// --- Status (informasi, bukan aksen) ---
val SuccessGreen = Color(0xFF047857)         // uang cukup, selesai, stok aman
val SuccessGreenLight = Color(0xFF10B981)
val SuccessGreenContainer = Color(0xFFE7F5EF)

val WarningAmber = Color(0xFFB45309)         // stok menipis, uang kurang, ditahan
val WarningAmberContainer = Color(0xFFFDF1E3)

val DangerRed = Color(0xFFB91C1C)            // hapus, void, stok habis, error
val DangerRedContainer = Color(0xFFFDECEC)

val InfoBlue = Color(0xFF0369A1)             // informatif netral (refund)
val InfoBlueContainer = Color(0xFFEAF4FC)
