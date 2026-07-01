package com.qlodi.cashpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.qlodi.cashpilot.AppState
import com.qlodi.cashpilot.data.api.*
import com.qlodi.cashpilot.ui.i18n.AppLanguage
import com.qlodi.cashpilot.ui.i18n.CashStrings
import com.qlodi.cashpilot.ui.i18n.LocalLanguage
import com.qlodi.cashpilot.ui.i18n.LocalStrings
import com.qlodi.cashpilot.ui.i18n.accountName
import com.qlodi.cashpilot.ui.i18n.errorText
import com.qlodi.cashpilot.ui.i18n.subtypeLabel
import com.qlodi.cashpilot.ui.components.*
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.theme.Radii
import com.qlodi.cashpilot.ui.theme.Spacing
import com.qlodi.cashpilot.ui.util.filterDateInput
import com.qlodi.cashpilot.ui.util.filterDecimalInput
import com.qlodi.cashpilot.ui.util.formatMoney
import com.qlodi.cashpilot.ui.util.normalizeDecimal
import com.qlodi.cashpilot.ui.util.parseAmount
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

private fun money(s: String) = formatMoney(s.toDoubleOrNull() ?: 0.0, "UAH")

/* ───────────── Chart of Accounts ───────────── */

@Composable
fun ChartOfAccountsScreen(state: AppState) {
    val c = CashpilotColors
    val S = LocalStrings.current
    val uk = LocalLanguage.current == AppLanguage.Ukrainian
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        SectionTitle(S.navAccounts, "${S.accountsPlanSub} · ${state.accounts.size} ${S.accountsCountWord}")
        if (state.accounts.isEmpty()) {
            if (state.busy) LoadingState() else EmptyState(Icons.Filled.Assessment, S.noAccounts, S.noAccountsSub)
            return@Column
        }
        AccountType.entries.forEach { type ->
            val group = state.accounts.filter { it.type == type }
            if (group.isEmpty()) return@forEach
            Text(typeLabel(type, S), color = c.textSecondary, style = MaterialTheme.typography.titleSmall)
            QCard(Modifier.fillMaxWidth(), padding = 0) {
                Column {
                    group.forEachIndexed { i, a ->
                        if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
                        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                            Text(a.code, color = c.heroCyan, style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(56.dp))
                            Text(accountName(a.code, a.name, uk), color = c.textPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            a.subtype?.let { QBadge(subtypeLabel(it, uk), c.textMuted) }
                            Spacer(Modifier.width(Spacing.sm))
                            Text(if (a.normalBalance == NormalBalance.DEBIT) S.dt else S.kt, color = c.textMuted, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(Spacing.huge))
    }
}

/* ───────────── Journal ───────────── */

@Composable
fun JournalScreen(state: AppState) {
    val c = CashpilotColors
    val S = LocalStrings.current
    val uk = LocalLanguage.current == AppLanguage.Ukrainian
    val scope = rememberCoroutineScope()
    var showEditor by remember { mutableStateOf(false) }
    var reverseTarget by remember { mutableStateOf<JournalEntryView?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(S.navJournal, "${state.entries.size} ${S.entriesWord}")
            Spacer(Modifier.weight(1f))
            if (showEditor) QTonalButton(S.close, { showEditor = false })
            else QPrimaryButton(S.newEntry, { showEditor = true })
        }

        if (showEditor) NewEntryEditor(state) { showEditor = false }

        if (state.entries.isEmpty() && !showEditor) {
            if (state.busy) LoadingState() else EmptyState(Icons.AutoMirrored.Filled.ReceiptLong, S.journalEmpty, S.journalEmptySub)
        }

        state.entries.forEach { e ->
            QCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(e.entryDate, color = c.textPrimary, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.width(Spacing.sm))
                        Text(e.description ?: "—", color = c.textSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (e.status == EntryStatus.REVERSED) QBadge(S.reversedBadge, c.warning)
                        else if (e.source == EntrySource.REVERSAL) QBadge(S.reversalBadge, c.textMuted)
                    }
                    e.lines.forEach { ln ->
                        Row(Modifier.fillMaxWidth().padding(start = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                            Text(ln.accountCode, color = c.heroCyan, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(48.dp))
                            Text(accountName(ln.accountCode, ln.accountName, uk), color = c.textSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            val dr = ln.direction == Direction.DEBIT
                            NumberText(money(ln.amountFunc), color = if (dr) c.textPrimary else c.textMuted, size = 12)
                            Spacer(Modifier.width(Spacing.xs))
                            Text(if (dr) S.dt else S.kt, color = c.textMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(20.dp))
                        }
                    }
                    if (e.status == EntryStatus.POSTED && e.source != EntrySource.REVERSAL) {
                        QTextLinkButton(S.reverseAction, { reverseTarget = e }, color = c.danger, leading = Icons.Filled.Undo)
                    }
                }
            }
        }
        Spacer(Modifier.height(Spacing.huge))
    }

    reverseTarget?.let { e ->
        ConfirmDialog(
            title = S.reverseConfirmTitle,
            message = S.reverseConfirmMsg,
            confirmLabel = S.reverseConfirm, destructive = true,
            onConfirm = { reverseTarget = null; scope.launch { state.reverse(e.id) } },
            onDismiss = { reverseTarget = null },
        )
    }
}

private class LineDraft(account: AccountView?, direction: Direction, amount: String) {
    var account by mutableStateOf(account)
    var direction by mutableStateOf(direction)
    var amount by mutableStateOf(amount)
}

@Composable
private fun NewEntryEditor(state: AppState, onDone: () -> Unit) {
    val c = CashpilotColors
    val S = LocalStrings.current
    val scope = rememberCoroutineScope()
    var date by remember { mutableStateOf("2026-06-30") }
    var desc by remember { mutableStateOf("") }
    val lines = remember { mutableStateListOf(LineDraft(null, Direction.DEBIT, ""), LineDraft(null, Direction.CREDIT, "")) }
    var err by remember { mutableStateOf<String?>(null) }
    var posting by remember { mutableStateOf(false) }

    val dr = lines.filter { it.direction == Direction.DEBIT }.sumOf { parseAmount(it.amount) }
    val cr = lines.filter { it.direction == Direction.CREDIT }.sumOf { parseAmount(it.amount) }
    val balanced = dr > 0 && kotlin.math.abs(dr - cr) < 0.005

    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text(S.newEntry, color = c.textPrimary, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                QTextField(date, { date = filterDateInput(it) }, S.date, Modifier.width(150.dp), keyboardType = KeyboardType.Number, placeholder = "2026-06-30")
                QTextField(desc, { desc = it }, S.description, Modifier.weight(1f))
            }
            lines.forEachIndexed { i, l ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    AccountPicker(state.accounts, l.account, Modifier.weight(1f)) { l.account = it }
                    DirToggle(l.direction) { l.direction = it }
                    QTextField(l.amount, { l.amount = filterDecimalInput(it) }, S.amount, Modifier.width(110.dp), keyboardType = KeyboardType.Decimal, placeholder = "0.00")
                    if (lines.size > 2) QTextLinkButton("✕", { lines.removeAt(i) }, color = c.textMuted)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                QTextLinkButton(S.addLine, { lines.add(LineDraft(null, Direction.DEBIT, "")) })
                Spacer(Modifier.weight(1f))
                Text("${S.dt} ${formatMoney(dr, "UAH")}  ·  ${S.kt} ${formatMoney(cr, "UAH")}",
                    color = if (balanced) c.positive else c.warning, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(Spacing.sm))
                QBadge(if (balanced) S.balancedBadge else S.unbalancedBadge, if (balanced) c.positive else c.warning)
            }
            err?.let { Text(S.errorText(it), color = c.danger, style = MaterialTheme.typography.bodyMedium) }
            QPrimaryButton(if (posting) "…" else S.post, enabled = balanced && !posting, modifier = Modifier.fillMaxWidth(), onClick = {
                scope.launch {
                    posting = true; err = null
                    val req = PostEntryRequest(
                        entryDate = date.trim(), description = desc.trim().ifBlank { null },
                        lines = lines.mapNotNull { l -> l.account?.let { JournalLineRequest(it.id, l.direction, normalizeDecimal(l.amount)) } },
                    )
                    err = state.post(req)
                    posting = false
                    if (err == null) onDone()
                }
            })
        }
    }
}

/** Пошуковий пікер рахунку через Dialog (надійний фокус/клавіатура на wasm, на відміну від DropdownMenu). */
@Composable
private fun AccountPicker(accounts: List<AccountView>, selected: AccountView?, modifier: Modifier = Modifier, onPick: (AccountView) -> Unit) {
    val c = CashpilotColors
    val S = LocalStrings.current
    val uk = LocalLanguage.current == AppLanguage.Ukrainian
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(Radii.sm)).background(c.surfaceHigh)
                .border(1.dp, c.border, RoundedCornerShape(Radii.sm)).clickable { open = true }
                .padding(horizontal = Spacing.md, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(selected?.let { "${it.code} ${accountName(it.code, it.name, uk)}" } ?: S.accountHint,
                color = if (selected != null) c.textPrimary else c.textMuted, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        }
    }
    if (open) {
        var query by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { open = false }) {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(Radii.lg)).background(c.surface)
                    .border(1.dp, c.border, RoundedCornerShape(Radii.lg)).padding(Spacing.lg),
            ) {
                Column(Modifier.heightIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(S.pickAccount, color = c.textPrimary, style = MaterialTheme.typography.titleMedium)
                    QTextField(query, { query = it }, S.search,
                        trailingIcon = { Icon(Icons.Filled.Search, null, tint = c.textMuted) })
                    Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                        accounts.filter { query.isBlank() || it.code.contains(query, true) || it.name.contains(query, true) || accountName(it.code, it.name, uk).contains(query, true) }.forEach { a ->
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(Radii.sm)).clickable { onPick(a); open = false }
                                    .padding(horizontal = Spacing.md, vertical = Spacing.md),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(a.code, color = c.heroCyan, style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(52.dp))
                                Text(accountName(a.code, a.name, uk), color = c.textPrimary, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DirToggle(dir: Direction, onChange: (Direction) -> Unit) {
    val c = CashpilotColors
    val S = LocalStrings.current
    Row(Modifier.clip(RoundedCornerShape(Radii.sm)).background(c.surfaceHigh).border(1.dp, c.border, RoundedCornerShape(Radii.sm))) {
        listOf(Direction.DEBIT to S.dt, Direction.CREDIT to S.kt).forEach { (d, label) ->
            val on = d == dir
            Box(
                Modifier.clip(RoundedCornerShape(Radii.sm)).background(if (on) c.primary else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onChange(d) }.heightIn(min = 48.dp).padding(horizontal = Spacing.md).wrapContentHeight(),
                contentAlignment = Alignment.Center,
            ) { Text(label, color = if (on) c.onPrimary else c.textMuted, style = MaterialTheme.typography.labelLarge) }
        }
    }
}

/* ───────────── Reports ───────────── */

@Composable
fun ReportsScreen(state: AppState) {
    val c = CashpilotColors
    val S = LocalStrings.current
    var tab by remember { mutableStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        SectionTitle(S.navReports, S.reportsSub)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            listOf(S.trialBalance, S.balanceSheet, S.cashFlow).forEachIndexed { i, t ->
                val on = i == tab
                Box(Modifier.clip(RoundedCornerShape(Radii.pill)).background(if (on) c.accentDim else c.surface)
                    .border(1.dp, if (on) c.heroCyan else c.border, RoundedCornerShape(Radii.pill))
                    .clickable { tab = i }.heightIn(min = 40.dp).padding(horizontal = Spacing.lg).wrapContentHeight()) {
                    Text(t, color = if (on) c.heroCyan else c.textSecondary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        when (tab) {
            0 -> TrialBalanceCard(state.trialBalance)
            1 -> BalanceSheetCard(state.balanceSheet)
            else -> CashFlowCard(state.cashFlow)
        }
        Spacer(Modifier.height(Spacing.huge))
    }
}

@Composable
private fun CashFlowCard(cf: CashFlowView?) {
    val c = CashpilotColors
    val S = LocalStrings.current
    if (cf == null) { EmptyState(Icons.Filled.Assessment, S.noData, S.addEntries); return }
    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            CfRow(S.cfOpening, cf.openingCash, c.textMuted)
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
            CfRow(S.cfOperating, cf.operating, c.textPrimary)
            CfRow(S.cfInvesting, cf.investing, c.textPrimary)
            CfRow(S.cfFinancing, cf.financing, c.textPrimary)
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
            CfRow(S.cfNetChange, cf.netChange, if ((cf.netChange.toDoubleOrNull() ?: 0.0) >= 0) c.positive else c.danger, bold = true)
            CfRow(S.cfClosing, cf.closingCash, c.textPrimary, bold = true)
        }
    }
}

@Composable
private fun CfRow(label: String, amount: String, color: androidx.compose.ui.graphics.Color, bold: Boolean = false) {
    val c = CashpilotColors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = if (bold) c.textPrimary else c.textSecondary,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        NumberText(money(amount), color = color, size = if (bold) 14 else 13, weight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun TrialBalanceCard(tb: TrialBalanceView?) {
    val c = CashpilotColors
    val S = LocalStrings.current
    val uk = LocalLanguage.current == AppLanguage.Ukrainian
    if (tb == null) { EmptyState(Icons.Filled.Assessment, S.noData, S.addEntries); return }
    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row {
                Text(S.accountCol, color = c.textMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text(S.dt, color = c.textMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.5f))
                Text(S.kt, color = c.textMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.5f))
            }
            tb.rows.forEach { r ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(accountName(r.code, r.name, uk), color = c.textPrimary, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        Text(r.code, color = c.heroCyan, style = MaterialTheme.typography.labelSmall)
                    }
                    NumberText(if ((r.debit.toDoubleOrNull() ?: 0.0) > 0) money(r.debit) else "—", size = 12, modifier = Modifier.weight(0.5f))
                    NumberText(if ((r.credit.toDoubleOrNull() ?: 0.0) > 0) money(r.credit) else "—", size = 12, modifier = Modifier.weight(0.5f))
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(S.totalRow, color = c.textPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                NumberText(money(tb.totalDebit), size = 12, weight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                NumberText(money(tb.totalCredit), size = 12, weight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
            }
            QBadge(if (tb.balanced) "${S.sumDtKt} ✓" else S.unbalancedBadge, if (tb.balanced) c.positive else c.danger)
        }
    }
}

@Composable
private fun BalanceSheetCard(bs: BalanceSheetView?) {
    val c = CashpilotColors
    val S = LocalStrings.current
    if (bs == null) { EmptyState(Icons.Filled.Assessment, S.noData, S.addEntries); return }
    QCard(Modifier.fillMaxWidth()) {
        val uk = LocalLanguage.current == AppLanguage.Ukrainian
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            BsSection(S.assets, bs.assets, bs.totalAssets, S.assetsTotal, uk)
            Spacer(Modifier.height(Spacing.xs))
            BsSection(S.liabilities, bs.liabilities, null, null, uk)
            BsSection(S.equity, bs.equity, null, null, uk)
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
            Row {
                Text(S.liabEquityTotal, color = c.textPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                NumberText(money(bs.totalLiabilitiesEquity), size = 13, weight = FontWeight.Bold)
            }
            QBadge(if (bs.balanced) "${S.aEqLE} ✓" else S.bsNotBalanced, if (bs.balanced) c.positive else c.danger)
        }
    }
}

@Composable
private fun BsSection(title: String, lines: List<BalanceSheetLine>, total: String?, totalLabel: String?, uk: Boolean) {
    val c = CashpilotColors
    Text(title, color = c.textSecondary, style = MaterialTheme.typography.titleSmall)
    lines.forEach { l ->
        val nm = if (l.code == "—") (if (uk) "Поточний фінрезультат (P&L)" else "Current result (P&L)") else accountName(l.code, l.name, uk)
        Row(Modifier.padding(start = Spacing.xs)) {
            Text(if (l.code == "—") nm else "${l.code}  $nm", color = c.textPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            NumberText(money(l.amount), size = 12)
        }
    }
    if (total != null && totalLabel != null) Row {
        Text(totalLabel, color = c.textPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        NumberText(money(total), size = 13, weight = FontWeight.Bold)
    }
}

private fun typeLabel(t: AccountType, s: CashStrings) = when (t) {
    AccountType.ASSET -> s.assets; AccountType.LIABILITY -> s.liabilities
    AccountType.EQUITY -> s.equity; AccountType.INCOME -> s.income; AccountType.EXPENSE -> s.expense
}
