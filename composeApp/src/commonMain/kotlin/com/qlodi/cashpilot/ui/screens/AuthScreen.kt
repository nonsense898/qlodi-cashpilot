package com.qlodi.cashpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qlodi.cashpilot.AppState
import com.qlodi.cashpilot.ui.components.QPrimaryButton
import com.qlodi.cashpilot.ui.components.QTextField
import com.qlodi.cashpilot.ui.components.QTextLinkButton
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.theme.Radii
import com.qlodi.cashpilot.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(state: AppState) {
    val c = CashpilotColors
    val scope = rememberCoroutineScope()
    var register by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    fun submit() {
        scope.launch {
            if (register) state.register(email.trim(), password, name.trim().ifBlank { null })
            else state.login(email.trim(), password)
        }
    }

    Box(Modifier.fillMaxSize().background(c.background), contentAlignment = Alignment.Center) {
        Column(
            Modifier.widthIn(max = 400.dp).fillMaxWidth().padding(Spacing.xl)
                .clip(RoundedCornerShape(Radii.lg)).background(c.surface)
                .border(1.dp, c.border, RoundedCornerShape(Radii.lg)).padding(Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(c.heroCyan), contentAlignment = Alignment.Center) {
                    Text("Q", color = c.onAccent, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                }
                Spacer(Modifier.width(Spacing.md))
                Column {
                    Text("Qlodi CashPilot", color = c.textPrimary, style = MaterialTheme.typography.titleMedium)
                    Text("облік для фаундерів", color = c.textMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(Spacing.xs))
            Text(if (register) "Створити акаунт" else "Вхід", color = c.textPrimary, style = MaterialTheme.typography.headlineSmall)

            if (register) QTextField(name, { name = it }, "Імʼя", placeholder = "Alex Kovalenko")
            QTextField(email, { email = it }, "Email", keyboardType = KeyboardType.Email, placeholder = "you@email.com")
            QTextField(password, { password = it }, "Пароль", password = true, keyboardType = KeyboardType.Password)

            state.error?.let { Text(it, color = c.danger, style = MaterialTheme.typography.bodyMedium) }

            QPrimaryButton(
                text = if (state.busy) "…" else if (register) "Створити" else "Увійти",
                onClick = ::submit, enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(if (register) "Вже є акаунт?" else "Немає акаунта?", color = c.textMuted, style = MaterialTheme.typography.bodyMedium)
                QTextLinkButton(if (register) "Увійти" else "Реєстрація", onClick = { register = !register; state.error = null })
            }
        }
    }
}
