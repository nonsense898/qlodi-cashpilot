package com.qlodi.cashpilot.ui.util

import androidx.compose.runtime.Composable

// Заглушка (первинна ціль — web). Нативний file-picker додається пізніше.
@Composable
actual fun rememberCsvPickerState(onText: (String?) -> Unit): CsvPickerState = CsvPickerState(pick = {})
