package com.qlodi.cashpilot.data.api

// Нативний Android не «перезавантажується» як веб-сторінка, а persist сесії
// потребував би Context. Лишаємо no-op — поведінка як була (in-memory сесія).
actual fun saveSessionJson(json: String?) {}

actual fun loadSessionJson(): String? = null
