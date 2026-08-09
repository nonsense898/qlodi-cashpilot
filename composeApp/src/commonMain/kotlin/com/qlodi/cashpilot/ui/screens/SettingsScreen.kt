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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.CurrencyExchange
import com.qlodi.cashpilot.AppState
import com.qlodi.cashpilot.data.api.ApiResult
import com.qlodi.cashpilot.data.api.FxRateView
import com.qlodi.cashpilot.data.api.SessionStore
import com.qlodi.cashpilot.data.api.UpsertFxRateRequest
import com.qlodi.cashpilot.ui.util.filterDateInput
import com.qlodi.cashpilot.ui.util.filterDecimalInput
import com.qlodi.cashpilot.ui.util.todayIsoDate
import com.qlodi.cashpilot.ui.components.*
import com.qlodi.cashpilot.ui.i18n.AppLanguage
import com.qlodi.cashpilot.ui.i18n.LocalLanguage
import com.qlodi.cashpilot.ui.i18n.LocalStrings
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.theme.Radii
import com.qlodi.cashpilot.ui.theme.Spacing
import com.qlodi.cashpilot.ui.theme.ThemeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: AppState) {
    val c = CashpilotColors
    val S = LocalStrings.current
    val lang = LocalLanguage.current
    val scope = rememberCoroutineScope()
    var langPicker by remember { mutableStateOf(false) }
    var currencySheet by remember { mutableStateOf(false) }
    var currencyErr by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var showSwitch by remember { mutableStateOf(false) }
    var showFx by remember { mutableStateOf(false) }

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

        // ── Company: switch between legal entities, edit currency, add new ──
        SectionLabel(S.companyGroup)
        QCard(Modifier.fillMaxWidth(), padding = 0) {
            Column {
                if (state.entities.size > 1) {
                    SettingsRow(Icons.Filled.SwapHoriz, S.switchCompany, trailingText = "${state.entities.size}", showChevron = true, onClick = { showSwitch = true })
                    RowDivider()
                }
                SettingsRow(Icons.Filled.Edit, S.editCompany, trailingText = state.entity?.functionalCurrency ?: "—", showChevron = true, onClick = { showEdit = true })
                RowDivider()
                SettingsRow(Icons.Filled.Add, S.addCompany, showChevron = true, onClick = { showAdd = true })
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
                SettingsRow(Icons.Filled.AccountBalanceWallet, S.currencyLabel, trailingText = state.entity?.functionalCurrency ?: "—", showChevron = true, onClick = { currencyErr = null; currencySheet = true })
                RowDivider()
                SettingsRow(Icons.Filled.Public, S.jurisdictionLabel, trailingText = state.entity?.jurisdiction ?: "—")
                RowDivider()
                SettingsRow(Icons.Filled.CurrencyExchange, S.fxRates, showChevron = true, onClick = { showFx = true })
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

    // ── Currency picker (modal bottom sheet) — changes the active company currency ──
    if (currencySheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(onDismissRequest = { currencySheet = false }, sheetState = sheetState, containerColor = c.surface) {
            Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.lg).padding(bottom = Spacing.huge), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(S.currencyLabel, color = c.textPrimary, style = MaterialTheme.typography.titleMedium)
                Text(S.currencyLockedHint, color = c.textMuted, style = MaterialTheme.typography.bodySmall)
                currencyErr?.let { Text(it, color = c.danger, style = MaterialTheme.typography.bodySmall) }
                Spacer(Modifier.height(Spacing.xs))
                CURRENCY_OPTIONS.forEach { (code, name) ->
                    val on = code == state.entity?.functionalCurrency
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(Radii.sm))
                            .background(if (on) c.accentDim else Color.Transparent)
                            .clickable {
                                scope.launch {
                                    val e = state.updateCompany(null, code)
                                    if (e == null) currencySheet = false else currencyErr = e
                                }
                            }
                            .padding(horizontal = Spacing.md, vertical = Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(code, color = if (on) c.heroCyan else c.textPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(56.dp))
                        Text(name, color = c.textSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (on) Text("✓", color = c.heroCyan, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    // ── Switch active company ──
    if (showSwitch) {
        CompanyDialogShell(S.switchCompany, onDismiss = { showSwitch = false }) {
            state.entities.forEach { e ->
                val on = e.id == state.entity?.id
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(Radii.sm))
                        .background(if (on) c.accentDim else Color.Transparent)
                        .clickable { scope.launch { state.selectEntity(e.id) }; showSwitch = false }
                        .padding(horizontal = Spacing.md, vertical = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(e.name, color = if (on) c.heroCyan else c.textPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Text("${e.jurisdiction} · ${e.functionalCurrency}", color = c.textMuted, style = MaterialTheme.typography.bodySmall)
                    if (on) { Spacer(Modifier.width(Spacing.sm)); Text("✓", color = c.heroCyan) }
                }
            }
        }
    }

    // ── Add company (name + jurisdiction + currency) ──
    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var jur by remember { mutableStateOf("UA") }
        var cur by remember { mutableStateOf("UAH") }
        var err by remember { mutableStateOf<String?>(null) }
        CompanyDialogShell(S.addCompany, onDismiss = { showAdd = false }) {
            QTextField(name, { name = it }, S.companyName, Modifier.fillMaxWidth())
            PillRow(S.jurisdictionLabel, listOf("UA", "EU"), jur) { jur = it; cur = if (it == "EU") "EUR" else "UAH" }
            PillRow(S.currencyLabel, CURRENCIES, cur) { cur = it }
            err?.let { Text(it, color = c.danger, style = MaterialTheme.typography.bodySmall) }
            QPrimaryButton(
                S.createAction,
                onClick = { scope.launch { err = state.createCompany(name, cur, jur); if (err == null) showAdd = false } },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank(),
            )
        }
    }

    // ── Edit active company (rename + currency, locked once entries exist) ──
    if (showEdit) {
        val cur0 = state.entity?.functionalCurrency ?: "UAH"
        var name by remember { mutableStateOf(state.entity?.name ?: "") }
        var cur by remember { mutableStateOf(cur0) }
        var err by remember { mutableStateOf<String?>(null) }
        CompanyDialogShell(S.editCompany, onDismiss = { showEdit = false }) {
            QTextField(name, { name = it }, S.companyName, Modifier.fillMaxWidth())
            PillRow(S.currencyLabel, CURRENCIES, cur) { cur = it }
            Text(S.currencyLockedHint, color = c.textMuted, style = MaterialTheme.typography.bodySmall)
            err?.let { Text(it, color = c.danger, style = MaterialTheme.typography.bodySmall) }
            QPrimaryButton(
                S.saveAction,
                onClick = {
                    scope.launch {
                        err = state.updateCompany(name.trim(), cur.takeIf { it != cur0 })
                        if (err == null) showEdit = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank(),
            )
        }
    }

    // ── FX rates: historical rates used to translate currencies in group reports ──
    if (showFx) {
        var fxList by remember { mutableStateOf<List<FxRateView>>(emptyList()) }
        var base by remember { mutableStateOf(state.entity?.functionalCurrency ?: "UAH") }
        var quote by remember { mutableStateOf("USD") }
        var date by remember { mutableStateOf(todayIsoDate()) }
        var rate by remember { mutableStateOf("") }
        var err by remember { mutableStateOf<String?>(null) }
        suspend fun reload() { fxList = state.api.listFxRates().getOrNull().orEmpty() }
        LaunchedEffect(Unit) { reload() }
        CompanyDialogShell(S.fxRates, onDismiss = { showFx = false }) {
            Text(S.fxHint, color = c.textMuted, style = MaterialTheme.typography.bodySmall)
            PillRow(S.currencyLabel, CURRENCIES, base) { base = it }
            PillRow(S.presentationCurrency, CURRENCIES, quote) { quote = it }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Box(Modifier.weight(1f)) { QTextField(date, { date = filterDateInput(it) }, S.date, Modifier.fillMaxWidth()) }
                Box(Modifier.weight(1f)) { QTextField(rate, { rate = filterDecimalInput(it) }, S.fxRateValue, Modifier.fillMaxWidth()) }
            }
            err?.let { Text(it, color = c.danger, style = MaterialTheme.typography.bodySmall) }
            QPrimaryButton(
                S.fxAdd,
                onClick = {
                    scope.launch {
                        err = null
                        when (val r = state.api.upsertFxRate(UpsertFxRateRequest(base, quote, date.trim(), rate.trim()))) {
                            is ApiResult.Ok -> { rate = ""; reload() }
                            is ApiResult.Err -> err = r.error.message
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = base != quote && rate.isNotBlank(),
            )
            if (fxList.isEmpty()) {
                Text(S.fxEmpty, color = c.textMuted, style = MaterialTheme.typography.bodySmall)
            } else {
                fxList.take(12).forEach { fr ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${fr.base}→${fr.quote}", color = c.textPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(88.dp))
                        Text(fr.rateDate, color = c.textMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                        Text(fr.rate, color = c.heroCyan, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

private val CURRENCIES = listOf("UAH", "USD", "EUR", "PLN", "GBP")

private val CURRENCY_OPTIONS = listOf(
    "UAH" to "Ukrainian hryvnia",
    "USD" to "US dollar",
    "EUR" to "Euro",
    "GBP" to "Pound sterling",
    "PLN" to "Polish zloty",
    "CHF" to "Swiss franc",
    "CAD" to "Canadian dollar",
    "JPY" to "Japanese yen",
)

@Composable
private fun CompanyDialogShell(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val c = CashpilotColors
    Dialog(onDismissRequest = onDismiss) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(Radii.lg)).background(c.surface).border(1.dp, c.border, RoundedCornerShape(Radii.lg)).padding(Spacing.lg)) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(title, color = c.textPrimary, style = MaterialTheme.typography.titleMedium)
                content()
            }
        }
    }
}

/** Horizontal single-select pill row (label + chips). */
@Composable
private fun PillRow(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    val c = CashpilotColors
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(label, color = c.textMuted, style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            options.forEach { opt ->
                val on = opt == selected
                Box(
                    Modifier.clip(RoundedCornerShape(Radii.sm))
                        .background(if (on) c.heroCyan else c.surfaceElevated)
                        .clickable { onSelect(opt) }
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                ) {
                    Text(opt, color = if (on) c.onAccent else c.textSecondary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
