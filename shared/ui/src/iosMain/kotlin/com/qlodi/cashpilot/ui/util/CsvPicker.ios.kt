package com.qlodi.cashpilot.ui.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberCsvPickerState(onText: (String?) -> Unit): CsvPickerState = CsvPickerState(pick = {})
