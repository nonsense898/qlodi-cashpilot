package com.qlodi.cashpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.qlodi.cashpilot.AppState
import com.qlodi.cashpilot.data.api.*
import com.qlodi.cashpilot.ui.components.*
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.theme.Spacing
import com.qlodi.cashpilot.ui.util.formatMoney
import kotlinx.coroutines.launch

private fun amt(s: String) = s.toDoubleOrNull() ?: 0.0

private fun balanceOf(state: AppState, acc: AccountView): Double {
    var net = 0.0
    state.entries.filter { it.status == EntryStatus.POSTED }.forEach { e ->
        e.lines.filter { it.accountId == acc.id }.forEach { ln ->
            net += if (ln.direction == Direction.DEBIT) amt(ln.amountFunc) else -amt(ln.amountFunc)
        }
    }
    return if (acc.normalBalance == NormalBalance.DEBIT) net else -net
}

/* ───────────── Banking ───────────── */

@Composable
fun BankingScreen(state: AppState) {
    val c = CashpilotColors
    val cash = state.accounts.filter { it.subtype in setOf("CASH", "BANK", "BANK_FX") }
    val total = cash.sumOf { balanceOf(state, it) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        SectionTitle("Banking", "Грошова позиція")
        if (cash.isEmpty()) { EmptyState(Icons.Filled.AccountBalance, "Немає рахунків", "Грошові рахунки зʼявляться з плану рахунків."); return@Column }
        QCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text("Усього на рахунках", color = c.textMuted, style = MaterialTheme.typography.bodyMedium)
                NumberText(formatMoneyUah(total), size = 28)
            }
        }
        QCard(Modifier.fillMaxWidth(), padding = 0) {
            Column {
                cash.forEachIndexed { i, a ->
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
                    Row(Modifier.fillMaxWidth().padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                        Text(a.code, color = c.heroCyan, style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(48.dp))
                        Text(a.name, color = c.textPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        NumberText(formatMoneyUah(balanceOf(state, a)), size = 14)
                    }
                }
            }
        }
        Text("Reconciliation (фіди банку) — наступний крок.", color = c.textMuted, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(Spacing.huge))
    }
}

/* ───────────── Taxes / VAT ───────────── */

@Composable
fun TaxesScreen(state: AppState) {
    val c = CashpilotColors
    val payable = state.accBySub("VAT_PAYABLE")?.let { balanceOf(state, it) } ?: 0.0
    val outTransit = state.accBySub("VAT_OUTPUT_TRANSIT")?.let { balanceOf(state, it) } ?: 0.0
    val inTransit = state.accBySub("VAT_INPUT_TRANSIT")?.let { balanceOf(state, it) } ?: 0.0
    val net = outTransit + payable - inTransit
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        SectionTitle("Taxes / VAT", "ПДВ 20% · транзит 643/644")
        QCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text("ПДВ до сплати (нетто)", color = c.textMuted, style = MaterialTheme.typography.bodyMedium)
                NumberText(formatMoneyUah(net), size = 28, color = if (net > 0) c.warning else c.positive)
            }
        }
        QCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                VatRow("643 Податкові зобовʼязання (output)", outTransit)
                VatRow("6411 Розрахунки за ПДВ", payable)
                VatRow("644 Податковий кредит (input)", -inTransit)
            }
        }
        Text("UA-VAT-20 · 14 · 7 · 0 · exempt · NA — повний рушій далі.", color = c.textMuted, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(Spacing.huge))
    }
}

@Composable
private fun VatRow(label: String, value: Double) {
    val c = CashpilotColors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = c.textSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        NumberText(formatMoneyUah(value), size = 13)
    }
}

/* ───────────── Periods ───────────── */

@Composable
fun PeriodsScreen(state: AppState) {
    val c = CashpilotColors
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        SectionTitle("Periods", "Закриття / лок періодів")
        if (state.periods.isEmpty()) {
            EmptyState(Icons.Filled.CalendarMonth, "Немає періодів", "Зʼявляться після першої проводки.")
            return@Column
        }
        state.periods.forEach { p ->
            QCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${p.periodStart} … ${p.periodEnd}", color = c.textPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        val (txt, col) = when (p.status) {
                            PeriodStatus.OPEN -> "OPEN" to c.positive
                            PeriodStatus.SOFT_CLOSED -> "SOFT-CLOSED" to c.warning
                            PeriodStatus.LOCKED -> "LOCKED" to c.danger
                        }
                        QBadge(txt, col)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        if (p.status != PeriodStatus.OPEN) QTonalButton("Відкрити", { scope.launch { state.setPeriod(p.id, "open") } })
                        if (p.status == PeriodStatus.OPEN) QTonalButton("Soft-close", { scope.launch { state.setPeriod(p.id, "soft-close") } })
                        if (p.status != PeriodStatus.LOCKED) QDangerButton("Lock", { scope.launch { state.setPeriod(p.id, "lock") } })
                    }
                }
            }
        }
        Spacer(Modifier.height(Spacing.huge))
    }
}

/* ───────────── Invoices (AR) / Bills (AP) ───────────── */

@Composable
fun InvoicesScreen(state: AppState) = DocScreen(
    state, title = "Invoices (AR)", subtitle = "Рахунки клієнтам · дохід + ПДВ",
    counterpartyLabel = "Клієнт", touchSubtype = "AR", ctaLabel = "Інвойс",
    emptyIcon = Icons.AutoMirrored.Filled.ReceiptLong, emptyTitle = "Немає інвойсів",
) { date, who, net, vat ->
    val ar = state.accBySub("AR") ?: return@DocScreen "Немає рахунку AR"
    val rev = state.accBySub("REVENUE") ?: return@DocScreen "Немає рахунку доходу"
    val vatAcc = state.accBySub("VAT_OUTPUT_TRANSIT")
    val total = net + vat
    val lines = buildList {
        add(JournalLineRequest(ar.id, Direction.DEBIT, total.toString()))
        add(JournalLineRequest(rev.id, Direction.CREDIT, net.toString()))
        if (vat > 0 && vatAcc != null) add(JournalLineRequest(vatAcc.id, Direction.CREDIT, vat.toString()))
    }
    state.post(PostEntryRequest(entryDate = date, description = "Інвойс · $who", source = EntrySource.AR, lines = lines))
}

@Composable
fun BillsScreen(state: AppState) = DocScreen(
    state, title = "Bills (AP)", subtitle = "Рахунки постачальників · витрата + ПДВ",
    counterpartyLabel = "Постачальник", touchSubtype = "AP", ctaLabel = "Рахунок",
    emptyIcon = Icons.Filled.RequestQuote, emptyTitle = "Немає рахунків",
) { date, who, net, vat ->
    val ap = state.accBySub("AP") ?: return@DocScreen "Немає рахунку AP"
    val exp = state.accBySub("ADMIN") ?: state.accBySub("COGS") ?: return@DocScreen "Немає рахунку витрат"
    val vatAcc = state.accBySub("VAT_INPUT_TRANSIT")
    val total = net + vat
    val lines = buildList {
        add(JournalLineRequest(exp.id, Direction.DEBIT, net.toString()))
        if (vat > 0 && vatAcc != null) add(JournalLineRequest(vatAcc.id, Direction.DEBIT, vat.toString()))
        add(JournalLineRequest(ap.id, Direction.CREDIT, total.toString()))
    }
    state.post(PostEntryRequest(entryDate = date, description = "Рахунок · $who", source = EntrySource.AP, lines = lines))
}

@Composable
private fun DocScreen(
    state: AppState, title: String, subtitle: String, counterpartyLabel: String,
    touchSubtype: String, ctaLabel: String, emptyIcon: androidx.compose.ui.graphics.vector.ImageVector, emptyTitle: String,
    onSubmit: suspend (date: String, who: String, net: Double, vat: Double) -> String?,
) {
    val c = CashpilotColors
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    val accIds = state.accounts.filter { it.subtype == touchSubtype }.map { it.id }.toSet()
    val docs = state.entries.filter { e -> e.lines.any { it.accountId in accIds } }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(title, subtitle)
            Spacer(Modifier.weight(1f))
            if (open) QTonalButton("Закрити", { open = false }) else QPrimaryButton(ctaLabel, { open = true })
        }
        if (open) DocForm(counterpartyLabel) { date, who, net, vat ->
            scope.launch { if (onSubmit(date, who, net, vat) == null) open = false }
        }
        if (docs.isEmpty() && !open) EmptyState(emptyIcon, emptyTitle, "Створіть перший через кнопку вгорі.")
        docs.forEach { e ->
            QCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(e.description ?: "—", color = c.textPrimary, style = MaterialTheme.typography.bodyLarge)
                        Text(e.entryDate, color = c.textMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    val total = e.lines.filter { it.accountId in accIds }.sumOf { amt(it.amountFunc) }
                    NumberText(formatMoneyUah(total), size = 14)
                }
            }
        }
        Spacer(Modifier.height(Spacing.huge))
    }
}

@Composable
private fun DocForm(counterpartyLabel: String, onSubmit: (String, String, Double, Double) -> Unit) {
    val c = CashpilotColors
    var date by remember { mutableStateOf("2026-06-30") }
    var who by remember { mutableStateOf("") }
    var net by remember { mutableStateOf("") }
    var withVat by remember { mutableStateOf(true) }
    val netD = net.toDoubleOrNull() ?: 0.0
    val vat = if (withVat) netD * 0.2 else 0.0

    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                QTextField(date, { date = it }, "Дата", Modifier.width(150.dp))
                QTextField(who, { who = it }, counterpartyLabel, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                QTextField(net, { net = it }, "Сума (нетто)", Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                FilterChip(
                    selected = withVat, onClick = { withVat = !withVat },
                    label = { Text("ПДВ 20%") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = c.heroCyan.copy(alpha = 0.16f), selectedLabelColor = c.heroCyan,
                        containerColor = c.surfaceHigh, labelColor = c.textMuted,
                    ),
                )
            }
            Text("Разом: ${formatMoneyUah(netD + vat)}  (ПДВ ${formatMoneyUah(vat)})", color = c.textSecondary, style = MaterialTheme.typography.bodySmall)
            QPrimaryButton("Провести", onClick = { onSubmit(date.trim(), who.trim(), netD, vat) }, enabled = netD > 0 && who.isNotBlank(), modifier = Modifier.fillMaxWidth())
        }
    }
}

private fun formatMoneyUah(v: Double) = formatMoney(v, "UAH")
