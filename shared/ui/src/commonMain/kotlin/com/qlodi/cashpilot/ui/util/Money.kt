package com.qlodi.cashpilot.ui.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Форматування грошей (порт підмножини frc-personal `Formatters.kt`).
 * ВАЖЛИВО: UAH → текст «грн» (після числа), бо ₴ (U+20B4) рендериться як tofu
 * на Skia/web/iOS. Символ — з ISO-коду, роздільник тисяч — нерозривний пробіл.
 */
private data class CurrencyMeta(val symbol: String, val symbolBefore: Boolean, val fractionDigits: Int)

private val META: Map<String, CurrencyMeta> = mapOf(
    "USD" to CurrencyMeta("$", true, 2),
    "EUR" to CurrencyMeta("€", true, 2),
    "GBP" to CurrencyMeta("£", true, 2),
    "UAH" to CurrencyMeta("грн", false, 2),
    "PLN" to CurrencyMeta("zł", false, 2),
)

private fun meta(code: String) = META[code.uppercase()] ?: CurrencyMeta(code.uppercase(), true, 2)

/** Символ валюти для коду (UAH→"грн"). */
fun currencySymbolFor(code: String): String = meta(code).symbol

/**
 * formatMoney(1248300.0, "UAH") -> "1 248 300,00 грн"
 * formatMoney(420000.0, "USD")  -> "$420,000.00"
 */
fun formatMoney(amount: Double, code: String = "UAH", compact: Boolean = false): String {
    val cm = meta(code)
    val uk = code.uppercase() == "UAH"
    val thousands = if (uk) " " else ","
    val decimal = if (uk) "," else "."
    val sign = if (amount < 0) "-" else ""
    val a = abs(amount)

    val numberPart = if (compact && a >= 1000) {
        when {
            a >= 1_000_000 -> grouped(a / 1_000_000, 1, thousands, decimal) + "M"
            else -> grouped(a / 1_000, 1, thousands, decimal) + "K"
        }
    } else grouped(a, cm.fractionDigits, thousands, decimal)

    return if (cm.symbolBefore) "$sign${cm.symbol}$numberPart" else "$sign$numberPart ${cm.symbol}"
}

private fun grouped(value: Double, fractionDigits: Int, thousands: String, decimal: String): String {
    val factor = 10.0.pow(fractionDigits)
    val rounded = (value * factor).roundToLong()
    val intPart = (rounded / factor.toLong()).toString()
    val frac = if (fractionDigits == 0) "" else
        decimal + (rounded % factor.toLong()).toString().padStart(fractionDigits, '0')
    val sb = StringBuilder()
    val n = intPart.length
    for (i in 0 until n) {
        if (i > 0 && (n - i) % 3 == 0) sb.append(thousands)
        sb.append(intPart[i])
    }
    return sb.toString() + frac
}
