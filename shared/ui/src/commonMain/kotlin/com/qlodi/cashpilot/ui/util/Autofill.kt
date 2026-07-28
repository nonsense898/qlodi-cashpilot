package com.qlodi.cashpilot.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Тип поля для менеджера паролів. Мапиться на HTML-атрибут autocomplete:
 * Email → username, CurrentPassword → current-password,
 * NewPassword/ConfirmPassword → new-password.
 */
enum class AutofillKind { Email, CurrentPassword, NewPassword, ConfirmPassword }

/** Візуальні параметри, щоб DOM-поле візуально не відрізнялося від Compose-дизайну. */
data class AutofillStyle(
    val textColor: Color,
    val hintColor: Color,
    val caretColor: Color,
    val fontSizeSp: Int = 14,
)

/**
 * Чи потребує платформа DOM-оверлея для збережених паролів.
 *
 * true лише на web: Compose малює в <canvas> усередині shadow-root, у якому
 * менеджерам паролів (Chrome, Safari Keychain, 1Password) нема за що зачепитися —
 * вони шукають справжні <input autocomplete=...> у DOM. На Android/iOS
 * автозаповнення дає системний API, тож там false і працює звичайний BasicTextField.
 */
expect val platformUsesDomAutofill: Boolean

/**
 * Web: справжній <input> поверх Compose-поля — він і приймає ввід, і малює текст,
 * каретку та плейсхолдер (нативні виділення й мобільна клавіатура «безкоштовно»).
 * Compose при цьому лишає за собою рамку, іконку й решту оформлення.
 * На нативних платформах — no-op: викликач малює звичайний BasicTextField.
 */
@Composable
expect fun DomAutofillField(
    kind: AutofillKind,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    passwordVisible: Boolean,
    style: AutofillStyle,
    onSubmit: () -> Unit,
    modifier: Modifier,
)

/**
 * Запропонувати браузеру зберегти пару email/пароль після успішного входу
 * (Credential Management API; Chromium). Safari/Firefox показують власний
 * промпт за фактом заповненої форми — там це no-op.
 */
expect fun saveCredentialToBrowser(email: String, password: String)

/**
 * Тихий вхід збереженою парою: браузер показує вибір акаунта й повертає
 * пару в [onCredential]. Працює лише там, де є PasswordCredential (Chromium).
 */
expect fun tryBrowserAutoSignIn(onCredential: (email: String, password: String) -> Unit)
