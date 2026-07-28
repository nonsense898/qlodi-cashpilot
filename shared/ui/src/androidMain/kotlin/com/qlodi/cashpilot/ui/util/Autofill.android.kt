package com.qlodi.cashpilot.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Android: автозаповнення дає системний Autofill Framework (AutofillNode в AuthInput). */
actual val platformUsesDomAutofill: Boolean = false

@Composable
actual fun DomAutofillField(
    kind: AutofillKind,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    passwordVisible: Boolean,
    style: AutofillStyle,
    onSubmit: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier,
) = Unit

actual fun saveCredentialToBrowser(email: String, password: String) = Unit

actual fun tryBrowserAutoSignIn(onCredential: (String, String) -> Unit) = Unit
