package com.qlodi.cashpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.qlodi.cashpilot.AppState
import com.qlodi.cashpilot.data.api.SessionStore
import com.qlodi.cashpilot.ui.components.*
import com.qlodi.cashpilot.ui.i18n.AppLanguage
import com.qlodi.cashpilot.ui.i18n.LocalLanguage
import com.qlodi.cashpilot.ui.i18n.LocalStrings
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.theme.Radii
import com.qlodi.cashpilot.ui.theme.Spacing
import com.qlodi.cashpilot.ui.theme.ThemeState

@Composable
fun SettingsScreen(state: AppState) {
    val c = CashpilotColors
    val S = LocalStrings.current
    val lang = LocalLanguage.current
    var langPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionTitle(S.navSettings)

        // Profile card
        QCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(Radii.md)).background(c.heroCyan), contentAlignment = Alignment.Center) {
                    Text((state.entity?.name?.firstOrNull() ?: 'C').uppercase(), color = c.onAccent, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(state.entity?.name ?: "—", color = c.textPrimary, style = MaterialTheme.typography.titleMedium)
                    Text(SessionStore.email ?: "—", color = c.textMuted, style = MaterialTheme.typography.bodyMedium)
                }
                state.entity?.let { QBadge("${it.jurisdiction} · ${it.functionalCurrency}") }
            }
        }

        SectionLabel(S.settingsGroup)
        QCard(Modifier.fillMaxWidth(), padding = 0) {
            Column {
                SettingsRow(Icons.Filled.DarkMode, S.themeLabel, trailing = {
                    Switch(
                        checked = ThemeState.dark, onCheckedChange = { ThemeState.dark = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = c.primary, checkedThumbColor = c.onPrimary),
                    )
                })
                RowDivider()
                SettingsRow(Icons.Filled.Language, S.language, trailingText = lang.label, showChevron = true, onClick = { langPicker = true })
                RowDivider()
                SettingsRow(Icons.Filled.AccountBalanceWallet, S.currencyLabel, trailingText = state.entity?.functionalCurrency ?: "—")
                RowDivider()
                SettingsRow(Icons.Filled.Public, S.jurisdictionLabel, trailingText = state.entity?.jurisdiction ?: "—")
            }
        }

        SectionLabel(S.accountGroup)
        QCard(Modifier.fillMaxWidth(), padding = 0) {
            SettingsRow(Icons.AutoMirrored.Filled.Logout, S.navLogout, tint = c.danger, onClick = { state.logout() })
        }
        Spacer(Modifier.height(Spacing.huge))
    }

    if (langPicker) {
        Dialog(onDismissRequest = { langPicker = false }) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(Radii.lg)).background(c.surface).border(1.dp, c.border, RoundedCornerShape(Radii.lg)).padding(Spacing.lg)) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Text(S.selectLanguage, color = c.textPrimary, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(Spacing.xs))
                    AppLanguage.entries.forEach { l ->
                        val on = l == lang
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(Radii.sm))
                                .background(if (on) c.accentDim else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable { state.setLanguage(l); langPicker = false }
                                .padding(horizontal = Spacing.md, vertical = Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(l.label, color = if (on) c.heroCyan else c.textPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(44.dp))
                            Text(if (l == AppLanguage.Ukrainian) "Українська" else "English", color = c.textSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            if (on) Text("✓", color = c.heroCyan, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
