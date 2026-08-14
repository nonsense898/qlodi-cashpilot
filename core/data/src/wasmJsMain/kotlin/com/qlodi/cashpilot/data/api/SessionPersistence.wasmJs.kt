@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.qlodi.cashpilot.data.api

// Прямий JS-інтероп до localStorage — без залежності від kotlinx-browser.
// try/catch: приватний режим / вимкнене сховище не має ронити застосунок.
@JsFun(
    "(v) => { try { if (v === null) localStorage.removeItem('cashpilot.session'); " +
        "else localStorage.setItem('cashpilot.session', v); } catch (e) {} }"
)
private external fun lsSave(v: String?)

@JsFun("() => { try { return localStorage.getItem('cashpilot.session'); } catch (e) { return null; } }")
private external fun lsLoad(): String?

actual fun saveSessionJson(json: String?) = lsSave(json)

actual fun loadSessionJson(): String? = lsLoad()
