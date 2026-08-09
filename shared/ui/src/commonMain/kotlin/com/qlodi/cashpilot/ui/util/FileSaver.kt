package com.qlodi.cashpilot.ui.util

/**
 * Зберігає текст як файл, що завантажується (web: Blob + <a download>).
 * На нативі — заглушка (первинна ціль — web), як і [rememberCsvPickerState].
 */
expect fun saveTextFile(fileName: String, content: String, mime: String = "text/csv;charset=utf-8")
