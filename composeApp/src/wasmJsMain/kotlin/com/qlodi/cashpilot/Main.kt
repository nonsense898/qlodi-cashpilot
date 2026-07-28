package com.qlodi.cashpilot

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window

@JsFun("(s) => decodeURIComponent(s)")
private external fun decodeURIComponent(s: String): String

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Дев-оверрайд бекенду: ?api=http://localhost:8099 (локальний стек).
    window.location.search
        .removePrefix("?").split("&")
        .firstOrNull { it.startsWith("api=") }
        ?.substringAfter("api=")
        ?.takeIf { it.isNotBlank() }
        ?.let { com.qlodi.cashpilot.data.api.ApiConfig.baseUrl = decodeURIComponent(it) }

    ComposeViewport(
        viewportContainer = document.getElementById("composeRoot")!!,
        configure = { isA11YEnabled = false },
    ) {
        App()
    }
}
