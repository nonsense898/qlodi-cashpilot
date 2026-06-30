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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.qlodi.cashpilot.ui.components.*
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.theme.Radii
import com.qlodi.cashpilot.ui.theme.Spacing
import com.qlodi.cashpilot.ui.util.formatMoney
import kotlinx.coroutines.launch

private fun money(s: String) = formatMoney(s.toDoubleOrNull() ?: 0.0, "UAH")

/* ───────────── Chart of Accounts ───────────── */

@Composable
fun ChartOfAccountsScreen(state: AppState) {
    val c = CashpilotColors
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        SectionTitle("Chart of Accounts", "План рахунків НП(С)БО · ${state.accounts.size} рахунків")
        if (state.accounts.isEmpty()) {
            if (state.busy) LoadingState() else EmptyState(Icons.Filled.Assessment, "Немає рахунків", "Створіть entity, щоб отримати дефолтний план рахунків.")
            return@Column
        }
        AccountType.entries.forEach { type ->
            val group = state.accounts.filter { it.type == type }
            if (group.isEmpty()) return@forEach
            Text(typeLabel(type), color = c.textSecondary, style = MaterialTheme.typography.titleSmall)
            QCard(Modifier.fillMaxWidth(), padding = 0) {
                Column {
                    group.forEachIndexed { i, a ->
                        if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
                        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                            Text(a.code, color = c.heroCyan, style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(56.dp))
                            Text(a.name, color = c.textPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            a.subtype?.let { QBadge(it, c.textMuted) }
                            Spacer(Modifier.width(Spacing.sm))
                            Text(if (a.normalBalance == NormalBalance.DEBIT) "Дт" else "Кт", color = c.textMuted, style = MaterialTheme.typography.labelMedium)
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
    val scope = rememberCoroutineScope()
    var showEditor by remember { mutableStateOf(false) }
    var reverseTarget by remember { mutableStateOf<JournalEntryView?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Journal", "${state.entries.size} проводок")
            Spacer(Modifier.weight(1f))
            if (showEditor) QTonalButton("Закрити", { showEditor = false })
            else QPrimaryButton("Нова проводка", { showEditor = true })
        }

        if (showEditor) NewEntryEditor(state) { showEditor = false }

        if (state.entries.isEmpty() && !showEditor) {
            if (state.busy) LoadingState() else EmptyState(Icons.AutoMirrored.Filled.ReceiptLong, "Журнал порожній", "Створіть першу проводку — Σ Дт = Σ Кт.")
        }

        state.entries.forEach { e ->
            QCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(e.entryDate, color = c.textPrimary, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.width(Spacing.sm))
                        Text(e.description ?: "—", color = c.textSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (e.status == EntryStatus.REVERSED) QBadge("сторновано", c.warning)
                        else if (e.source == EntrySource.REVERSAL) QBadge("сторно", c.textMuted)
                    }
                    e.lines.forEach { ln ->
                        Row(Modifier.fillMaxWidth().padding(start = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                            Text(ln.accountCode, color = c.heroCyan, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(48.dp))
                            Text(ln.accountName, color = c.textSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            val dr = ln.direction == Direction.DEBIT
                            NumberText(money(ln.amountFunc), color = if (dr) c.textPrimary else c.textMuted, size = 12)
                            Spacer(Modifier.width(Spacing.xs))
                            Text(if (dr) "Дт" else "Кт", color = c.textMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(20.dp))
                        }
                    }
                    if (e.status == EntryStatus.POSTED && e.source != EntrySource.REVERSAL) {
                        QTextLinkButton("Сторнувати", { reverseTarget = e }, color = c.danger, leading = Icons.Filled.Undo)
                    }
                }
            }
        }
        Spacer(Modifier.height(Spacing.huge))
    }

    reverseTarget?.let { e ->
        ConfirmDialog(
            title = "Сторнувати проводку?",
            message = "Буде створено дзеркальну проводку; оригінал лишиться в журналі зі статусом REVERSED. Редагування неможливе — лише сторно.",
            confirmLabel = "Сторнувати", destructive = true,
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
    val scope = rememberCoroutineScope()
    var date by remember { mutableStateOf("2026-06-30") }
    var desc by remember { mutableStateOf("") }
    val lines = remember { mutableStateListOf(LineDraft(null, Direction.DEBIT, ""), LineDraft(null, Direction.CREDIT, "")) }
    var err by remember { mutableStateOf<String?>(null) }
    var posting by remember { mutableStateOf(false) }

    val dr = lines.filter { it.direction == Direction.DEBIT }.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    val cr = lines.filter { it.direction == Direction.CREDIT }.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    val balanced = dr > 0 && kotlin.math.abs(dr - cr) < 0.005

    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Нова проводка", color = c.textPrimary, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                QTextField(date, { date = it }, "Дата", Modifier.width(150.dp), placeholder = "2026-06-30")
                QTextField(desc, { desc = it }, "Опис", Modifier.weight(1f))
            }
            lines.forEachIndexed { i, l ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    AccountPicker(state.accounts, l.account, Modifier.weight(1f)) { l.account = it }
                    DirToggle(l.direction) { l.direction = it }
                    QTextField(l.amount, { l.amount = it }, "Сума", Modifier.width(110.dp), keyboardType = KeyboardType.Decimal, placeholder = "0.00")
                    if (lines.size > 2) QTextLinkButton("✕", { lines.removeAt(i) }, color = c.textMuted)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                QTextLinkButton("+ рядок", { lines.add(LineDraft(null, Direction.DEBIT, "")) })
                Spacer(Modifier.weight(1f))
                Text("Дт ${formatMoney(dr, "UAH")}  ·  Кт ${formatMoney(cr, "UAH")}",
                    color = if (balanced) c.positive else c.warning, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(Spacing.sm))
                QBadge(if (balanced) "збалансовано" else "Σ Дт ≠ Σ Кт", if (balanced) c.positive else c.warning)
            }
            err?.let { Text(it, color = c.danger, style = MaterialTheme.typography.bodyMedium) }
            QPrimaryButton(if (posting) "…" else "Провести", enabled = balanced && !posting, modifier = Modifier.fillMaxWidth(), onClick = {
                scope.launch {
                    posting = true; err = null
                    val req = PostEntryRequest(
                        entryDate = date.trim(), description = desc.trim().ifBlank { null },
                        lines = lines.mapNotNull { l -> l.account?.let { JournalLineRequest(it.id, l.direction, l.amount.trim()) } },
                    )
                    err = state.post(req)
                    posting = false
                    if (err == null) onDone()
                }
            })
        }
    }
}

/** Пошуковий пікер рахунку (39 рахунків — без пошуку незручно). */
@Composable
private fun AccountPicker(accounts: List<AccountView>, selected: AccountView?, modifier: Modifier = Modifier, onPick: (AccountView) -> Unit) {
    val c = CashpilotColors
    var open by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    Box(modifier) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(Radii.sm)).background(c.surfaceHigh)
                .border(1.dp, c.border, RoundedCornerShape(Radii.sm)).clickable { open = true; query = "" }
                .padding(horizontal = Spacing.md, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(selected?.let { "${it.code} ${it.name}" } ?: "Рахунок…",
                color = if (selected != null) c.textPrimary else c.textMuted, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false },
            modifier = Modifier.background(c.surface).heightIn(max = 360.dp).width(320.dp)) {
            QTextField(query, { query = it }, "Пошук рахунку", Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                keyboardType = KeyboardType.Text, trailingIcon = { Icon(Icons.Filled.Search, null, tint = c.textMuted) })
            accounts.filter { query.isBlank() || it.code.contains(query, true) || it.name.contains(query, true) }.forEach { a ->
                DropdownMenuItem(
                    text = { Text("${a.code}  ${a.name}", color = c.textPrimary, style = MaterialTheme.typography.bodyMedium) },
                    onClick = { onPick(a); open = false },
                )
            }
        }
    }
}

@Composable
private fun DirToggle(dir: Direction, onChange: (Direction) -> Unit) {
    val c = CashpilotColors
    Row(Modifier.clip(RoundedCornerShape(Radii.sm)).background(c.surfaceHigh).border(1.dp, c.border, RoundedCornerShape(Radii.sm))) {
        listOf(Direction.DEBIT to "Дт", Direction.CREDIT to "Кт").forEach { (d, label) ->
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
    var tab by remember { mutableStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        SectionTitle("Reports", "Деривуються з леджера")
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            listOf("Trial Balance", "Balance Sheet").forEachIndexed { i, t ->
                val on = i == tab
                Box(Modifier.clip(RoundedCornerShape(Radii.pill)).background(if (on) c.accentDim else c.surface)
                    .border(1.dp, if (on) c.heroCyan else c.border, RoundedCornerShape(Radii.pill))
                    .clickable { tab = i }.heightIn(min = 40.dp).padding(horizontal = Spacing.lg).wrapContentHeight()) {
                    Text(t, color = if (on) c.heroCyan else c.textSecondary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        if (tab == 0) TrialBalanceCard(state.trialBalance) else BalanceSheetCard(state.balanceSheet)
        Spacer(Modifier.height(Spacing.huge))
    }
}

@Composable
private fun TrialBalanceCard(tb: TrialBalanceView?) {
    val c = CashpilotColors
    if (tb == null) { EmptyState(Icons.Filled.Assessment, "Немає даних", "Додайте проводки."); return }
    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row {
                Text("Рахунок", color = c.textMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Дт", color = c.textMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.5f))
                Text("Кт", color = c.textMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.5f))
            }
            tb.rows.forEach { r ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(r.name, color = c.textPrimary, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        Text(r.code, color = c.heroCyan, style = MaterialTheme.typography.labelSmall)
                    }
                    NumberText(if ((r.debit.toDoubleOrNull() ?: 0.0) > 0) money(r.debit) else "—", size = 12, modifier = Modifier.weight(0.5f))
                    NumberText(if ((r.credit.toDoubleOrNull() ?: 0.0) > 0) money(r.credit) else "—", size = 12, modifier = Modifier.weight(0.5f))
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Разом", color = c.textPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                NumberText(money(tb.totalDebit), size = 12, weight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                NumberText(money(tb.totalCredit), size = 12, weight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
            }
            QBadge(if (tb.balanced) "Σ Дт = Σ Кт ✓" else "не збалансовано", if (tb.balanced) c.positive else c.danger)
        }
    }
}

@Composable
private fun BalanceSheetCard(bs: BalanceSheetView?) {
    val c = CashpilotColors
    if (bs == null) { EmptyState(Icons.Filled.Assessment, "Немає даних", "Додайте проводки."); return }
    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            BsSection("Активи", bs.assets, bs.totalAssets, "Активи разом")
            Spacer(Modifier.height(Spacing.xs))
            BsSection("Зобовʼязання", bs.liabilities, null, null)
            BsSection("Капітал", bs.equity, null, null)
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
            Row {
                Text("Пасиви разом", color = c.textPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                NumberText(money(bs.totalLiabilitiesEquity), size = 13, weight = FontWeight.Bold)
            }
            QBadge(if (bs.balanced) "A = L + E ✓" else "не збалансовано", if (bs.balanced) c.positive else c.danger)
        }
    }
}

@Composable
private fun BsSection(title: String, lines: List<BalanceSheetLine>, total: String?, totalLabel: String?) {
    val c = CashpilotColors
    Text(title, color = c.textSecondary, style = MaterialTheme.typography.titleSmall)
    lines.forEach { l ->
        Row(Modifier.padding(start = Spacing.xs)) {
            Text("${l.code}  ${l.name}", color = c.textPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            NumberText(money(l.amount), size = 12)
        }
    }
    if (total != null && totalLabel != null) Row {
        Text(totalLabel, color = c.textPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        NumberText(money(total), size = 13, weight = FontWeight.Bold)
    }
}

private fun typeLabel(t: AccountType) = when (t) {
    AccountType.ASSET -> "Активи"; AccountType.LIABILITY -> "Зобовʼязання"
    AccountType.EQUITY -> "Капітал"; AccountType.INCOME -> "Доходи"; AccountType.EXPENSE -> "Витрати"
}
