package com.qlodi.cashpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qlodi.cashpilot.AppState
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(state: AppState) {
    val c = CashpilotColors
    val scope = rememberCoroutineScope()
    var register by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(c.background), contentAlignment = Alignment.Center) {
        Column(
            Modifier.widthIn(max = 380.dp).fillMaxWidth().padding(24.dp)
                .clip(RoundedCornerShape(16.dp)).background(c.surface)
                .border(1.dp, c.border, RoundedCornerShape(16.dp)).padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(c.heroCyan), contentAlignment = Alignment.Center) {
                    Text("Q", color = c.onAccent, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text("CashPilot", color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Text(if (register) "Створити акаунт" else "Вхід", color = c.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            if (register) Field("Імʼя", name, { name = it })
            Field("Email", email, { email = it })
            Field("Пароль", password, { password = it }, password = true)

            state.error?.let { Text(it, color = c.danger, fontSize = 13.sp) }

            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(c.heroCyan).clickable(enabled = !state.busy) {
                        scope.launch {
                            if (register) state.register(email.trim(), password, name.trim().ifBlank { null })
                            else state.login(email.trim(), password)
                        }
                    }.padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (state.busy) "…" else if (register) "Створити" else "Увійти",
                    color = c.onAccent, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(if (register) "Вже є акаунт? " else "Немає акаунта? ", color = c.textMuted, fontSize = 13.sp)
                Text(if (register) "Увійти" else "Реєстрація", color = c.heroCyan, fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { register = !register; state.error = null })
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit, password: Boolean = false) {
    val c = CashpilotColors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = c.textSecondary, fontSize = 12.5.sp)
        BasicTextField(
            value = value, onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(color = c.textPrimary, fontSize = 15.sp),
            cursorBrush = SolidColor(c.heroCyan),
            visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(c.surfaceElevated)
                .border(1.dp, c.border, RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 13.dp),
        )
    }
}
