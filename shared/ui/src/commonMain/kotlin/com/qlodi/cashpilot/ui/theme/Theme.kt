package com.qlodi.cashpilot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Палітра (dark/light). Приглушений teal-cyan; чистий неон не вживаємо. */
data class CashColors(
    val background: Color, val surface: Color, val surfaceElevated: Color, val surfaceHigh: Color,
    val border: Color, val borderStrong: Color,
    val primary: Color, val onPrimary: Color, val primaryContainer: Color, val onPrimaryContainer: Color,
    val heroCyan: Color, val accentDim: Color,
    val textPrimary: Color, val textSecondary: Color, val textMuted: Color, val onAccent: Color,
    val positive: Color, val warning: Color, val danger: Color, val onDanger: Color,
    val tabBg: Color, val tabBorder: Color, val isDark: Boolean,
)

private val DarkColors = CashColors(
    background = Color(0xFF06121F), surface = Color(0xFF0C1C2E), surfaceElevated = Color(0xFF122638), surfaceHigh = Color(0xFF18324A),
    border = Color(0xFF21384E), borderStrong = Color(0xFF2C4A66),
    primary = Color(0xFF1FB8C9), onPrimary = Color(0xFF04161B), primaryContainer = Color(0xFF103A45), onPrimaryContainer = Color(0xFF8DE9F1),
    heroCyan = Color(0xFF57E1EC), accentDim = Color(0x2657E1EC),
    textPrimary = Color(0xFFE8F1F8), textSecondary = Color(0xFFA6BACB), textMuted = Color(0xFF7B90A4), onAccent = Color(0xFF04161B),
    positive = Color(0xFF34C98A), warning = Color(0xFFE3A94F), danger = Color(0xFFE5675F), onDanger = Color(0xFF2A0A0A),
    tabBg = Color(0xD90C1C2E), tabBorder = Color(0x0FFFFFFF), isDark = true,
)

private val LightColors = CashColors(
    background = Color(0xFFF3F7FB), surface = Color(0xFFFFFFFF), surfaceElevated = Color(0xFFFFFFFF), surfaceHigh = Color(0xFFEDF3F8),
    border = Color(0xFFDDE7F0), borderStrong = Color(0xFFC6D5E2),
    primary = Color(0xFF0E8A99), onPrimary = Color(0xFFFFFFFF), primaryContainer = Color(0xFFCFEFF3), onPrimaryContainer = Color(0xFF073C44),
    heroCyan = Color(0xFF0B7C8C), accentDim = Color(0x1A0B7C8C),
    textPrimary = Color(0xFF0D2137), textSecondary = Color(0xFF45617A), textMuted = Color(0xFF6B829A), onAccent = Color(0xFFFFFFFF),
    positive = Color(0xFF12936A), warning = Color(0xFFB57A1E), danger = Color(0xFFC7453C), onDanger = Color(0xFFFFFFFF),
    tabBg = Color(0xF2FFFFFF), tabBorder = Color(0x14000000), isDark = false,
)

/** Стан теми (light/dark) — світч у Settings. */
object ThemeState {
    var dark: Boolean by mutableStateOf(true)
}

/** Поточна палітра (реактивна — читає [ThemeState.dark]). */
val CashpilotColors: CashColors get() = if (ThemeState.dark) DarkColors else LightColors

val LocalCashpilotColors = staticCompositionLocalOf { DarkColors }
val LocalIsCompact = staticCompositionLocalOf { false }

val NumberFontFamily = FontFamily.Monospace

object Spacing {
    val xs: Dp = 4.dp; val sm: Dp = 8.dp; val md: Dp = 12.dp; val lg: Dp = 16.dp
    val xl: Dp = 20.dp; val xxl: Dp = 28.dp; val huge: Dp = 40.dp
}

object Radii {
    val sm: Dp = 10.dp; val md: Dp = 14.dp; val lg: Dp = 20.dp; val pill: Dp = 999.dp
}

private fun schemeFor(c: CashColors) = if (c.isDark) {
    darkColorScheme(
        primary = c.primary, onPrimary = c.onPrimary, primaryContainer = c.primaryContainer, onPrimaryContainer = c.onPrimaryContainer,
        secondary = c.heroCyan, onSecondary = c.onAccent, secondaryContainer = c.primaryContainer, onSecondaryContainer = c.onPrimaryContainer,
        background = c.background, onBackground = c.textPrimary, surface = c.surface, onSurface = c.textPrimary,
        surfaceVariant = c.surfaceElevated, onSurfaceVariant = c.textSecondary, error = c.danger, onError = c.onDanger,
        outline = c.border, outlineVariant = c.borderStrong,
    )
} else {
    lightColorScheme(
        primary = c.primary, onPrimary = c.onPrimary, primaryContainer = c.primaryContainer, onPrimaryContainer = c.onPrimaryContainer,
        secondary = c.heroCyan, onSecondary = c.onAccent, secondaryContainer = c.primaryContainer, onSecondaryContainer = c.onPrimaryContainer,
        background = c.background, onBackground = c.textPrimary, surface = c.surface, onSurface = c.textPrimary,
        surfaceVariant = c.surfaceElevated, onSurfaceVariant = c.textSecondary, error = c.danger, onError = c.onDanger,
        outline = c.border, outlineVariant = c.borderStrong,
    )
}

private val CashpilotShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(Radii.sm),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(Radii.md),
    large = androidx.compose.foundation.shape.RoundedCornerShape(Radii.lg),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
)

private fun cashpilotTypography(): Typography {
    val t = Typography()
    return t.copy(
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp, letterSpacing = (-0.01).sp),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        titleSmall = t.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
        bodyLarge = t.bodyLarge.copy(fontSize = 15.sp),
        bodyMedium = t.bodyMedium.copy(fontSize = 13.5.sp),
        bodySmall = t.bodySmall.copy(fontSize = 12.5.sp),
        labelLarge = t.labelLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
        labelMedium = t.labelMedium.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
        labelSmall = t.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 10.5.sp),
    )
}

val NumberTextStyle: TextStyle
    @Composable get() = MaterialTheme.typography.titleMedium.copy(fontFamily = NumberFontFamily)

@Composable
fun CashpilotTheme(content: @Composable () -> Unit) {
    val c = CashpilotColors
    MaterialTheme(
        colorScheme = schemeFor(c),
        typography = cashpilotTypography(),
        shapes = CashpilotShapes,
        content = content,
    )
}
