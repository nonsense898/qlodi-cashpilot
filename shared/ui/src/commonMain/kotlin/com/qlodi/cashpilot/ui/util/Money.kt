package com.qlodi.cashpilot.ui.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

/** Активна локаль форматування ("uk"/"en") — змінюється разом із мовою UI. */
object LocaleConfig {
    var locale: String by mutableStateOf("uk")
}

/**
 * Форматування грошей (порт підмножини frc-personal `Formatters.kt`).
 * ВАЖЛИВО: UAH → текст «грн»/«UAH» (₴ U+20B4 рендериться як tofu на Skia/web/iOS).
 * Роздільники й символ залежать від [LocaleConfig].
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

/** Нормалізує десятковий роздільник: `,`→`.` (UA-юзери вводять кому). */
fun normalizeDecimal(s: String): String = s.trim().replace(',', '.')

/** Парсить суму з урахуванням коми як роздільника; 0.0 якщо не число. */
fun parseAmount(s: String): Double = normalizeDecimal(s).toDoubleOrNull() ?: 0.0

/**
 * Фільтр вводу для грошових полів: лишає тільки цифри + один роздільник (`.` або `,`).
 * KeyboardType лише підказує клавіатуру (на web/desktop ігнорується) — фільтруємо самі.
 */
fun filterDecimalInput(raw: String): String {
    val sb = StringBuilder()
    var hasSep = false
    for (ch in raw) {
        when {
            ch.isDigit() -> sb.append(ch)
            (ch == '.' || ch == ',') && !hasSep -> { sb.append(ch); hasSep = true }
        }
    }
    return sb.toString()
}

/** Фільтр вводу для дати: лише цифри й `-`, максимум 10 символів (yyyy-MM-dd). */
fun filterDateInput(raw: String): String = raw.filter { it.isDigit() || it == '-' }.take(10)

/**
 * formatMoney(1248300.0, "UAH") -> "1 248 300,00 грн" (uk) / "1,248,300.00 UAH" (en)
 * formatMoney(420000.0, "USD")  -> "$420,000.00"
 */
fun formatMoney(amount: Double, code: String = "UAH", compact: Boolean = false): String {
    val cm = meta(code)
    val isUah = code.uppercase() == "UAH"
    val uk = LocaleConfig.locale == "uk"
    val thousands = if (uk) " " else ","
    val decimal = if (uk) "," else "."
    val symbol = if (isUah) (if (uk) "грн" else "UAH") else cm.symbol
    val symbolBefore = if (isUah) false else cm.symbolBefore
    val sign = if (amount < 0) "-" else ""
    val a = abs(amount)

    val numberPart = if (compact && a >= 1000) {
        when {
            a >= 1_000_000 -> grouped(a / 1_000_000, 1, thousands, decimal) + "M"
            else -> grouped(a / 1_000, 1, thousands, decimal) + "K"
        }
    } else grouped(a, cm.fractionDigits, thousands, decimal)

    return if (symbolBefore) "$sign$symbol$numberPart" else "$sign$numberPart $symbol"
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
