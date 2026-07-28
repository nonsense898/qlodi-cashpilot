package com.qlodi.cashpilot.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity

/*
 * Web-автозаповнення для Compose-канваса.
 *
 * Compose малює UI в <canvas> усередині shadow-root #composeRoot — у DOM немає
 * жодного <input>, тож менеджери паролів не бачать форми логіну взагалі.
 * Тут ми створюємо справжню <form> у light-DOM (не в shadow — там автофіл
 * підтримується ненадійно) з трьома полями й накладаємо потрібне поле рівно
 * на Compose-поле. Введення, каретка, виділення й мобільна клавіатура —
 * нативні; Compose лишає за собою рамку, іконку та решту оформлення.
 */

private const val SLOT_USER = "user"
private const val SLOT_PASS = "pass"
private const val SLOT_PASS2 = "pass2"

@JsFun(
    """() => {
    if (window.__qlodiAf) return;
    var st = { cb: {}, submit: {} };

    var css = document.createElement('style');
    css.textContent =
        '#qlodi-autofill input::placeholder{color:var(--qaf-ph,#8B9DAE);opacity:1}' +
        // Chrome малює власний фон на автозаповнених полях — гасимо, щоб не
        // світився жовтий прямокутник поверх темного Compose-поля.
        '#qlodi-autofill input:-webkit-autofill{-webkit-text-fill-color:var(--qaf-fg,#E8F1F8)!important;' +
        'transition:background-color 9999s ease-in-out 0s}';
    document.head.appendChild(css);

    var form = document.createElement('form');
    form.id = 'qlodi-autofill';
    form.setAttribute('autocomplete', 'on');
    form.action = '#';
    form.style.cssText = 'position:absolute;left:0;top:0;width:0;height:0;margin:0;padding:0;border:0;';
    form.addEventListener('submit', function (e) {
        e.preventDefault();
        var f = st.submit['pass'] || st.submit['user'];
        if (f) f();
    });

    function mk(slot, type, ac, name) {
        var i = document.createElement('input');
        i.id = 'qlodi-af-' + slot;
        i.type = type;
        i.name = name;
        i.setAttribute('autocomplete', ac);
        i.setAttribute('autocapitalize', 'off');
        i.setAttribute('autocorrect', 'off');
        i.spellcheck = false;
        i.style.cssText = 'position:fixed;left:-9999px;top:0px;width:1px;height:1px;opacity:0;pointer-events:none;';
        i.addEventListener('input', function () { var f = st.cb[slot]; if (f) f(i.value); });
        i.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') { e.preventDefault(); var f = st.submit[slot]; if (f) f(); }
        });
        form.appendChild(i);
        st[slot] = i;
    }
    mk('user', 'email', 'username', 'email');
    mk('pass', 'password', 'current-password', 'password');
    mk('pass2', 'password', 'new-password', 'confirm_password');

    document.body.appendChild(form);
    window.__qlodiAf = st;
}"""
)
private external fun afInit()

@JsFun(
    """(slot, x, y, w, h, font, fg, caret, hint, ph, ac, type) => {
    var st = window.__qlodiAf; if (!st) return;
    var i = st[slot]; if (!i) return;
    if (i.getAttribute('autocomplete') !== ac) i.setAttribute('autocomplete', ac);
    if (i.type !== type) i.type = type;
    if (i.placeholder !== ph) i.placeholder = ph;
    i.style.cssText = 'position:fixed;box-sizing:border-box;' +
        'left:' + x + 'px;top:' + y + 'px;width:' + w + 'px;height:' + h + 'px;' +
        'font-family:Inter,-apple-system,BlinkMacSystemFont,system-ui,sans-serif;' +
        'font-size:' + font + 'px;line-height:' + h + 'px;' +
        'color:' + fg + ';caret-color:' + caret + ';' +
        'background:transparent;border:0;outline:0;margin:0;padding:0;' +
        'opacity:1;pointer-events:auto;z-index:2147483000;-webkit-appearance:none;appearance:none;';
    i.style.setProperty('--qaf-ph', hint);
    i.style.setProperty('--qaf-fg', fg);
}"""
)
private external fun afPlace(
    slot: String, x: Double, y: Double, w: Double, h: Double, font: Int,
    fg: String, caret: String, hint: String, ph: String, ac: String, type: String,
)

/** Ховаємо за межі екрана, але лишаємо в DOM — інакше менеджер «загубить» форму. */
@JsFun(
    """(slot) => {
    var st = window.__qlodiAf; if (!st) return;
    var i = st[slot]; if (!i) return;
    i.style.cssText = 'position:fixed;left:-9999px;top:0px;width:1px;height:1px;opacity:0;pointer-events:none;';
}"""
)
private external fun afHide(slot: String)

@JsFun("(slot, cb) => { if (window.__qlodiAf) window.__qlodiAf.cb[slot] = cb; }")
private external fun afOnInput(slot: String, cb: (String) -> Unit)

@JsFun("(slot, cb) => { if (window.__qlodiAf) window.__qlodiAf.submit[slot] = cb; }")
private external fun afOnSubmit(slot: String, cb: () -> Unit)

@JsFun(
    """(slot, v) => {
    var st = window.__qlodiAf; var i = st && st[slot];
    if (!i || i.value === v) return;
    // Якщо поле у фокусі — зберігаємо позицію каретки, інакше вона стрибне в кінець.
    var focused = document.activeElement === i;
    var pos = focused ? i.selectionStart : null;
    i.value = v;
    if (focused && pos !== null) { try { i.setSelectionRange(pos, pos); } catch (e) {} }
}"""
)
private external fun afSetValue(slot: String, v: String)

@JsFun(
    """(email, pass) => {
    try {
        if (!window.PasswordCredential || !navigator.credentials) return;
        navigator.credentials.store(new PasswordCredential({ id: email, password: pass, name: email }));
    } catch (e) { /* менеджер відсутній або заблокований політикою — не критично */ }
}"""
)
private external fun afStore(email: String, pass: String)

@JsFun(
    """(cb) => {
    try {
        if (!window.PasswordCredential || !navigator.credentials) return;
        navigator.credentials.get({ password: true, mediation: 'optional' }).then(function (c) {
            if (c && c.password) cb(c.id, c.password);
        }).catch(function () {});
    } catch (e) {}
}"""
)
private external fun afGet(cb: (String, String) -> Unit)

actual val platformUsesDomAutofill: Boolean = true

private fun Color.css(): String =
    "rgba(${(red * 255).toInt()},${(green * 255).toInt()},${(blue * 255).toInt()},$alpha)"

private val AutofillKind.slot: String
    get() = when (this) {
        AutofillKind.Email -> SLOT_USER
        AutofillKind.CurrentPassword, AutofillKind.NewPassword -> SLOT_PASS
        AutofillKind.ConfirmPassword -> SLOT_PASS2
    }

private val AutofillKind.autocomplete: String
    get() = when (this) {
        AutofillKind.Email -> "username"
        AutofillKind.CurrentPassword -> "current-password"
        AutofillKind.NewPassword, AutofillKind.ConfirmPassword -> "new-password"
    }

@Composable
actual fun DomAutofillField(
    kind: AutofillKind,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    passwordVisible: Boolean,
    style: AutofillStyle,
    onSubmit: () -> Unit,
    modifier: Modifier,
) {
    val slot = kind.slot
    val density = LocalDensity.current.density
    val onValue = rememberUpdatedState(onValueChange)
    val onDone = rememberUpdatedState(onSubmit)
    // Останнє значення, що прийшло з DOM. Потрібне, щоб НЕ писати його назад:
    // рекомпозиція Compose відстає від набору, і зворотний запис застарілого
    // (коротшого) значення обрізав би текст і зсував каретку.
    val lastFromDom = remember { mutableStateOf<String?>(null) }

    DisposableEffect(slot) {
        afInit()
        afOnInput(slot) { v ->
            lastFromDom.value = v
            onValue.value(v)
        }
        afOnSubmit(slot) { onDone.value() }
        onDispose { afHide(slot) }
    }

    // У DOM пишемо лише зміни, що прийшли ЗЗОВНІ (автовхід, очистка полів при
    // зміні вкладки) — відлуння власного набору ігноруємо.
    LaunchedEffect(slot, value) {
        if (value != lastFromDom.value) afSetValue(slot, value)
    }

    val isPassword = kind != AutofillKind.Email
    val type = if (isPassword && !passwordVisible) "password" else if (isPassword) "text" else "email"

    Box(
        modifier.onGloballyPositioned { coords ->
            val b = coords.boundsInWindow()
            afPlace(
                slot,
                (b.left / density).toDouble(),
                (b.top / density).toDouble(),
                (b.width / density).toDouble(),
                (b.height / density).toDouble(),
                style.fontSizeSp,
                style.textColor.css(),
                style.caretColor.css(),
                style.hintColor.css(),
                placeholder,
                kind.autocomplete,
                type,
            )
        }
    )
}

actual fun saveCredentialToBrowser(email: String, password: String) {
    if (email.isNotBlank() && password.isNotBlank()) afStore(email, password)
}

actual fun tryBrowserAutoSignIn(onCredential: (String, String) -> Unit) {
    afInit()
    afGet { e, p -> onCredential(e, p) }
}
