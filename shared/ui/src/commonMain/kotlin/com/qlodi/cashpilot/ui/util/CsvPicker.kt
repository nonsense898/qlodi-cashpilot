package com.qlodi.cashpilot.ui.util

import androidx.compose.runtime.Composable

/** Пікер CSV-виписки: відкриває файловий діалог, повертає текст файлу. */
data class CsvPickerState(val pick: () -> Unit)

@Composable
expect fun rememberCsvPickerState(onText: (String?) -> Unit): CsvPickerState

/**
 * Парсер банк-виписки CSV: рядки `date,amount,description`.
 * - пропускає порожні рядки та заголовок (якщо 1-й стовпець — не дата);
 * - amount може мати кому як роздільник (нормалізується);
 * - повертає трійки (date, amount, description).
 */
fun parseBankCsv(text: String): List<Triple<String, String, String?>> {
    val out = mutableListOf<Triple<String, String, String?>>()
    text.split('\n', '\r').forEach { raw ->
        val line = raw.trim()
        if (line.isEmpty()) return@forEach
        val cols = line.split(',', ';').map { it.trim().trim('"') }
        if (cols.size < 2) return@forEach
        val date = cols[0]
        // заголовок / нечислова сума → пропустити
        if (!date.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) return@forEach
        val amount = normalizeDecimal(cols[1].replace(" ", ""))
        if (amount.toDoubleOrNull() == null) return@forEach
        val desc = cols.getOrNull(2)?.takeIf { it.isNotBlank() }
        out += Triple(date, amount, desc)
    }
    return out
}
