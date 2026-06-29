package com.qlodi.cashpilot.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Бренд-токени Qlodi CashPilot — dark + cyan (узгоджено з Qlodi Business).
 * Джерело: «Бриф для Claude Design», розд. 3.
 */
object CashpilotColors {
    val heroCyan = Color(0xFF00FFFF)   // акцент на темному, CTA, активні стани
    val cyanDeep = Color(0xFF0B7C8C)   // акцент на світлих поверхнях, лінки
    val navy = Color(0xFF0D2137)       // основний темний фон / заголовки
    val background = Color(0xFF0A1828)
    val surface = Color(0xFF0F2336)
    val surfaceElevated = Color(0xFF16314A)
    val border = Color(0xFF22384F)

    val textPrimary = Color(0xFFEAF2F8)
    val textSecondary = Color(0xFF9FB3C4)
    val textMuted = Color(0xFF647C92)
    val onAccent = Color(0xFF012027)

    // Семантика (як у P&L-інсайтах Business)
    val positive = Color(0xFF3FD68B)
    val warning = Color(0xFFF2B45C)
    val danger = Color(0xFFF26D6D)
}

val LocalCashpilotColors = staticCompositionLocalOf { CashpilotColors }

@Composable
fun CashpilotTheme(content: @Composable () -> Unit) {
    // Скелет: проста обгортка. Material3-схема/типографіка додаються пізніше.
    content()
}
