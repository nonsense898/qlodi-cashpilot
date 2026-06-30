package com.qlodi.cashpilot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/**
 * Бренд-токени Qlodi CashPilot — dark navy + cyan.
 * Узгоджено з референсним UI (standalone mockup) та дизайн-брифом (розд. 3).
 */
object CashpilotColors {
    val background = Color(0xFF06121F)      // найглибший фон
    val surface = Color(0xFF0A1B2E)         // панелі / nav
    val surfaceElevated = Color(0xFF0D2137) // картки
    val border = Color(0xFF1C3A55)

    val heroCyan = Color(0xFF00FFFF)        // CTA, активні стани
    val cyanSoft = Color(0xFF9BEAF0)
    val cyanDeep = Color(0xFF0B7C8C)        // лінки на світлому

    val textPrimary = Color(0xFFE6F0F6)
    val textSecondary = Color(0xFF8BA6BC)
    val textMuted = Color(0xFF5E7C93)
    val onAccent = Color(0xFF012027)

    val positive = Color(0xFF3FD79A)
    val warning = Color(0xFFF5B544)
    val danger = Color(0xFFFF6B6B)

    val accentDim = Color(0x2200FFFF)       // 13% cyan — активний фон таба

    // Floating bottom nav (1:1 із frc-personal docked pill)
    val tabBg = Color(0xC70D2137)           // surface @ ~0.78 alpha
    val tabBorder = Color(0x0FFFFFFF)        // білий @ 0.06
}

val LocalCashpilotColors = staticCompositionLocalOf { CashpilotColors }

/** Compact (телефонна) ширина < 600dp — provided з кореня App. */
val LocalIsCompact = staticCompositionLocalOf { false }

/** Моноширинні цифри (фінансові дані вирівнюються по розряду) — проксі IBM Plex Mono. */
val NumberFontFamily = FontFamily.Monospace

private val CashpilotScheme = darkColorScheme(
    primary = CashpilotColors.heroCyan,
    onPrimary = CashpilotColors.onAccent,
    background = CashpilotColors.background,
    onBackground = CashpilotColors.textPrimary,
    surface = CashpilotColors.surface,
    onSurface = CashpilotColors.textPrimary,
    surfaceVariant = CashpilotColors.surfaceElevated,
    onSurfaceVariant = CashpilotColors.textSecondary,
    error = CashpilotColors.danger,
    outline = CashpilotColors.border,
)

@Composable
fun CashpilotTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CashpilotScheme, content = content)
}
