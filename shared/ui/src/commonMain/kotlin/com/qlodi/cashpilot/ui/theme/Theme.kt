package com.qlodi.cashpilot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Бренд-токени Qlodi CashPilot — dark navy + cyan, приглушені для premium + AA-контрасту.
 * primary (приглушений teal-cyan) — для площин/кнопок; accent (мʼякший cyan) — для дрібних
 * акцентів (іконки, активні стани, коди рахунків). Чистий неон #00FFFF більше не вживаємо.
 */
object CashpilotColors {
    val background = Color(0xFF06121F)
    val surface = Color(0xFF0C1C2E)
    val surfaceElevated = Color(0xFF122638)
    val surfaceHigh = Color(0xFF18324A)        // поля вводу / підняті поверхні
    val border = Color(0xFF21384E)
    val borderStrong = Color(0xFF2C4A66)

    val primary = Color(0xFF1FB8C9)            // приглушений teal-cyan — fills/CTA
    val onPrimary = Color(0xFF04161B)
    val primaryContainer = Color(0xFF103A45)   // тональні активні поверхні
    val onPrimaryContainer = Color(0xFF8DE9F1)

    val heroCyan = Color(0xFF57E1EC)           // accent (мʼякший cyan) — іконки/активне
    val accentDim = Color(0x2657E1EC)          // ~15% accent — активний фон таба

    val textPrimary = Color(0xFFE8F1F8)
    val textSecondary = Color(0xFFA6BACB)
    val textMuted = Color(0xFF7B90A4)          // піднято для AA на surface
    val onAccent = Color(0xFF04161B)

    val positive = Color(0xFF34C98A)
    val warning = Color(0xFFE3A94F)
    val danger = Color(0xFFE5675F)
    val onDanger = Color(0xFF2A0A0A)

    // Floating bottom nav (структура 1:1 із frc-personal docked pill)
    val tabBg = Color(0xD90C1C2E)              // surface @ ~0.85
    val tabBorder = Color(0x0FFFFFFF)          // білий @ 0.06
}

val LocalCashpilotColors = staticCompositionLocalOf { CashpilotColors }

/** Compact (телефонна) ширина < 600dp — provided з кореня App. */
val LocalIsCompact = staticCompositionLocalOf { false }

/** Моноширинні цифри (фінансові дані вирівнюються по розряду). */
val NumberFontFamily = FontFamily.Monospace

/** Spacing scale (4-pt base). */
object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 28.dp
    val huge: Dp = 40.dp
}

/** Corner radii. */
object Radii {
    val sm: Dp = 10.dp
    val md: Dp = 14.dp
    val lg: Dp = 20.dp
    val pill: Dp = 999.dp
}

private val CashpilotScheme = darkColorScheme(
    primary = CashpilotColors.primary,
    onPrimary = CashpilotColors.onPrimary,
    primaryContainer = CashpilotColors.primaryContainer,
    onPrimaryContainer = CashpilotColors.onPrimaryContainer,
    secondary = CashpilotColors.heroCyan,
    onSecondary = CashpilotColors.onAccent,
    secondaryContainer = CashpilotColors.primaryContainer,
    onSecondaryContainer = CashpilotColors.onPrimaryContainer,
    background = CashpilotColors.background,
    onBackground = CashpilotColors.textPrimary,
    surface = CashpilotColors.surface,
    onSurface = CashpilotColors.textPrimary,
    surfaceVariant = CashpilotColors.surfaceElevated,
    onSurfaceVariant = CashpilotColors.textSecondary,
    error = CashpilotColors.danger,
    onError = CashpilotColors.onDanger,
    outline = CashpilotColors.border,
    outlineVariant = CashpilotColors.borderStrong,
)

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
        displaySmall = t.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.01).sp),
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

/** Стиль для фінансових чисел (tabular). */
val NumberTextStyle: TextStyle
    @Composable get() = MaterialTheme.typography.titleMedium.copy(fontFamily = NumberFontFamily)

@Composable
fun CashpilotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CashpilotScheme,
        typography = cashpilotTypography(),
        shapes = CashpilotShapes,
        content = content,
    )
}
