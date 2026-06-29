package com.qlodi.cashpilot

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(
        viewportContainer = document.body!!,
        configure = { isA11YEnabled = false },
    ) {
        App()
    }
}
