package com.qlodi.cashpilot.ui.util

actual fun saveTextFile(fileName: String, content: String, mime: String) {
    jsSaveFile(fileName, content, mime)
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun(
    "(fileName, content, mime) => {" +
        " const url = URL.createObjectURL(new Blob([content], { type: mime }));" +
        " const a = document.createElement('a');" +
        " a.href = url; a.download = fileName;" +
        " document.body.appendChild(a); a.click(); a.remove();" +
        " setTimeout(() => URL.revokeObjectURL(url), 1000);" +
        " }",
)
private external fun jsSaveFile(fileName: String, content: String, mime: String)
