package com.qlodi.cashpilot.ui.util

import androidx.compose.runtime.Composable

/** Пікер CSV-виписки: відкриває файловий діалог, повертає текст файлу. */
data class CsvPickerState(val pick: () -> Unit)

@Composable
expect fun rememberCsvPickerState(onText: (String?) -> Unit): CsvPickerState

/** Результат парсингу виписки: розпізнані рядки + скільки рядків пропущено. */
data class BankCsvResult(
    val rows: List<Triple<String, String, String?>>,
    val skipped: Int,
)

/**
 * Парсер банк-виписки CSV.
 * - роздільник визначається по файлу (`;` / `,` / таб) поза лапками;
 * - поля в лапках за RFC 4180 (кома/крапка з комою всередині лапок — частина поля);
 * - дата в будь-якій колонці у форматах yyyy-MM-dd, dd.MM.yyyy, dd/MM/yyyy,
 *   dd-MM-yyyy, yyyy/MM/dd → нормалізується до yyyy-MM-dd;
 * - сумою вважається перша числова колонка після дати (пробіли й кома-роздільник
 *   нормалізуються); решта непорожніх колонок склеюється в опис;
 * - заголовок і нерозібрані рядки рахуються у [BankCsvResult.skipped].
 */
fun parseBankCsv(text: String): BankCsvResult {
    val clean = text.removePrefix("﻿")
    val lines = clean.split('\n', '\r').map { it.trim() }.filter { it.isNotEmpty() }
    if (lines.isEmpty()) return BankCsvResult(emptyList(), 0)

    val delimiter = inferDelimiter(lines)
    val out = mutableListOf<Triple<String, String, String?>>()
    var skipped = 0

    lines.forEach { line ->
        val cols = splitCsvLine(line, delimiter)
        val dateIdx = cols.indexOfFirst { normalizeCsvDate(it) != null }
        if (dateIdx < 0) { skipped++; return@forEach }
        val date = normalizeCsvDate(cols[dateIdx])!!

        var amountIdx = -1
        for (i in cols.indices) {
            if (i == dateIdx) continue
            val v = normalizeDecimal(cols[i].replace(" ", "").replace(" ", ""))
            if (v.toDoubleOrNull() != null && v.isNotBlank()) { amountIdx = i; break }
        }
        if (amountIdx < 0) { skipped++; return@forEach }
        val amount = normalizeDecimal(cols[amountIdx].replace(" ", "").replace(" ", ""))

        val desc = cols.withIndex()
            .filter { (i, v) -> i != dateIdx && i != amountIdx && v.isNotBlank() }
            .joinToString(" · ") { it.value }
            .takeIf { it.isNotBlank() }
        out += Triple(date, amount, desc)
    }
    return BankCsvResult(out, skipped)
}

/** Роздільник з найбільшою кількістю входжень поза лапками на перших рядках. */
private fun inferDelimiter(lines: List<String>): Char {
    val counts = mutableMapOf(';' to 0, ',' to 0, '\t' to 0)
    lines.take(5).forEach { line ->
        var inQuotes = false
        line.forEach { ch ->
            when {
                ch == '"' -> inQuotes = !inQuotes
                !inQuotes && ch in counts -> counts[ch] = counts.getValue(ch) + 1
            }
        }
    }
    return counts.maxByOrNull { it.value }!!.let { if (it.value == 0) ',' else it.key }
}

/** RFC 4180: лапковані поля, подвоєна лапка `""` → `"`. */
private fun splitCsvLine(line: String, delimiter: Char): List<String> {
    val cols = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val ch = line[i]
        when {
            ch == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> { sb.append('"'); i++ }
            ch == '"' -> inQuotes = !inQuotes
            ch == delimiter && !inQuotes -> { cols += sb.toString().trim(); sb.clear() }
            else -> sb.append(ch)
        }
        i++
    }
    cols += sb.toString().trim()
    return cols
}

private val ISO = Regex("""(\d{4})-(\d{2})-(\d{2})""")
private val ISO_SLASH = Regex("""(\d{4})/(\d{2})/(\d{2})""")
private val DMY = Regex("""(\d{1,2})[./-](\d{1,2})[./-](\d{4})""")

/** Нормалізує дату до yyyy-MM-dd або null, якщо це не дата. dd/mm — день першим (UA/EU). */
fun normalizeCsvDate(raw: String): String? {
    val s = raw.trim()
    ISO.matchEntire(s)?.let { return validYmd(it.groupValues[1], it.groupValues[2], it.groupValues[3]) }
    ISO_SLASH.matchEntire(s)?.let { return validYmd(it.groupValues[1], it.groupValues[2], it.groupValues[3]) }
    DMY.matchEntire(s)?.let {
        return validYmd(it.groupValues[3], it.groupValues[2].padStart(2, '0'), it.groupValues[1].padStart(2, '0'))
    }
    return null
}

private fun validYmd(y: String, m: String, d: String): String? {
    val yi = y.toIntOrNull() ?: return null
    val mi = m.toIntOrNull() ?: return null
    val di = d.toIntOrNull() ?: return null
    if (mi !in 1..12) return null
    val leap = (yi % 4 == 0 && yi % 100 != 0) || yi % 400 == 0
    val maxDay = when (mi) {
        2 -> if (leap) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    if (di !in 1..maxDay) return null
    return "$y-$m-$d"
}
