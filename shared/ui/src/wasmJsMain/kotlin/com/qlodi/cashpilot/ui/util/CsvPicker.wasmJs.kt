package com.qlodi.cashpilot.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.document
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File

@Composable
actual fun rememberCsvPickerState(onText: (String?) -> Unit): CsvPickerState {
    val scope = rememberCoroutineScope()
    val cb = rememberUpdatedState(onText)
    return remember(scope) {
        CsvPickerState(pick = {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = ".csv,text/csv,text/plain"
            input.onchange = {
                val file = input.files?.item(0)
                if (file == null) scope.launch { cb.value(null) }
                else jsReadText(file) { text -> scope.launch { cb.value(text.ifBlank { null }) } }
                Unit
            }
            input.click()
        })
    }
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(file, cb) => { const r = new FileReader(); r.onload = () => cb(r.result || ''); r.onerror = () => cb(''); r.readAsText(file); }")
private external fun jsReadText(file: File, cb: (String) -> Unit)
