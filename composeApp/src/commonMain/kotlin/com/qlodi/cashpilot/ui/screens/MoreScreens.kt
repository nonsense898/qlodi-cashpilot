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
import com.qlodi.cashpilot.ui.i18n.AppLanguage
import com.qlodi.cashpilot.ui.i18n.LocalLanguage
import com.qlodi.cashpilot.ui.i18n.LocalStrings
import com.qlodi.cashpilot.ui.i18n.accountName
import com.qlodi.cashpilot.ui.components.*
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.theme.Spacing
import com.qlodi.cashpilot.ui.util.filterDateInput
import com.qlodi.cashpilot.ui.util.filterDecimalInput
import com.qlodi.cashpilot.ui.util.formatMoney
import com.qlodi.cashpilot.ui.util.parseAmount
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
    val S = LocalStrings.current
    val uk = LocalLanguage.current == AppLanguage.Ukrainian
    val cash = state.accounts.filter { it.subtype in setOf("CASH", "BANK", "BANK_FX") }
    val total = cash.sumOf { balanceOf(state, it) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        SectionTitle(S.navBanking, S.cashPosition)
        if (cash.isEmpty()) { EmptyState(Icons.Filled.AccountBalance, S.noCashAccounts, S.noCashAccountsSub); return@Column }
        QCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(S.totalOnAccounts, color = c.textMuted, style = MaterialTheme.typography.bodyMedium)
                NumberText(formatMoneyUah(total), size = 28)
            }
        }
        QCard(Modifier.fillMaxWidth(), padding = 0) {
            Column {
                cash.forEachIndexed { i, a ->
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
                    Row(Modifier.fillMaxWidth().padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                        Text(a.code, color = c.heroCyan, style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(48.dp))
                        Text(accountName(a.code, a.name, uk), color = c.textPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        NumberText(formatMoneyUah(balanceOf(state, a)), size = 14)
                    }
                }
            }
        }
        Text(S.reconSoon, color = c.textMuted, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(Spacing.huge))
    }
}

/* ───────────── Taxes / VAT ───────────── */

@Composable
fun TaxesScreen(state: AppState) {
    val c = CashpilotColors
    val S = LocalStrings.current
    val payable = state.accBySub("VAT_PAYABLE")?.let { balanceOf(state, it) } ?: 0.0
    val outTransit = state.accBySub("VAT_OUTPUT_TRANSIT")?.let { balanceOf(state, it) } ?: 0.0
    val inTransit = state.accBySub("VAT_INPUT_TRANSIT")?.let { balanceOf(state, it) } ?: 0.0
    val net = outTransit + payable - inTransit
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        SectionTitle(S.navTaxes, S.taxesSub)
        QCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(S.vatToPayNet, color = c.textMuted, style = MaterialTheme.typography.bodyMedium)
                NumberText(formatMoneyUah(net), size = 28, color = if (net > 0) c.warning else c.positive)
            }
        }
        QCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                VatRow(S.vat643, outTransit)
                VatRow(S.vat6411, payable)
                VatRow(S.vat644, -inTransit)
            }
        }
        Text(S.vatEngineSoon, color = c.textMuted, style = MaterialTheme.typography.bodySmall)
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
    val S = LocalStrings.current
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        SectionTitle(S.navPeriods, S.periodsSub)
        if (state.periods.isEmpty()) {
            EmptyState(Icons.Filled.CalendarMonth, S.noPeriods, S.noPeriodsSub)
            return@Column
        }
        state.periods.forEach { p ->
            QCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${p.periodStart} … ${p.periodEnd}", color = c.textPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        val (txt, col) = when (p.status) {
                            PeriodStatus.OPEN -> S.pOpen to c.positive
                            PeriodStatus.SOFT_CLOSED -> S.pSoftClosed to c.warning
                            PeriodStatus.LOCKED -> S.pLocked to c.danger
                        }
                        QBadge(txt, col)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        if (p.status != PeriodStatus.OPEN) QTonalButton(S.reopen, { scope.launch { state.setPeriod(p.id, "open") } })
                        if (p.status == PeriodStatus.OPEN) QTonalButton(S.softClose, { scope.launch { state.setPeriod(p.id, "soft-close") } })
                        if (p.status != PeriodStatus.LOCKED) QDangerButton(S.lock, { scope.launch { state.setPeriod(p.id, "lock") } })
                    }
                }
            }
        }
        Spacer(Modifier.height(Spacing.huge))
    }
}

/* ───────────── Invoices (AR) / Bills (AP) ───────────── */

@Composable
fun InvoicesScreen(state: AppState) {
    val S = LocalStrings.current
    DocScreen(
    state, title = S.navInvoices, subtitle = S.invoicesSub,
    counterpartyLabel = S.client, touchSubtype = "AR", ctaLabel = S.invoiceBtn,
    emptyIcon = Icons.AutoMirrored.Filled.ReceiptLong, emptyTitle = S.noInvoices,
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
    state.post(PostEntryRequest(entryDate = date, description = "${S.invoiceBtn} · $who", source = EntrySource.AR, lines = lines))
}
}

@Composable
fun BillsScreen(state: AppState) {
    val S = LocalStrings.current
    DocScreen(
    state, title = S.navBills, subtitle = S.billsSub,
    counterpartyLabel = S.vendor, touchSubtype = "AP", ctaLabel = S.billBtn,
    emptyIcon = Icons.Filled.RequestQuote, emptyTitle = S.noBills,
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
    state.post(PostEntryRequest(entryDate = date, description = "${S.billBtn} · $who", source = EntrySource.AP, lines = lines))
}
}

@Composable
private fun DocScreen(
    state: AppState, title: String, subtitle: String, counterpartyLabel: String,
    touchSubtype: String, ctaLabel: String, emptyIcon: androidx.compose.ui.graphics.vector.ImageVector, emptyTitle: String,
    onSubmit: suspend (date: String, who: String, net: Double, vat: Double) -> String?,
) {
    val c = CashpilotColors
    val S = LocalStrings.current
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    val accIds = state.accounts.filter { it.subtype == touchSubtype }.map { it.id }.toSet()
    val docs = state.entries.filter { e -> e.lines.any { it.accountId in accIds } }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(title, subtitle)
            Spacer(Modifier.weight(1f))
            if (open) QTonalButton(S.close, { open = false }) else QPrimaryButton(ctaLabel, { open = true })
        }
        if (open) DocForm(counterpartyLabel) { date, who, net, vat ->
            scope.launch { if (onSubmit(date, who, net, vat) == null) open = false }
        }
        if (docs.isEmpty() && !open) EmptyState(emptyIcon, emptyTitle, S.createFirst)
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
    val S = LocalStrings.current
    var date by remember { mutableStateOf("2026-06-30") }
    var who by remember { mutableStateOf("") }
    var net by remember { mutableStateOf("") }
    var withVat by remember { mutableStateOf(true) }
    val netD = parseAmount(net)
    val vat = if (withVat) netD * 0.2 else 0.0

    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                QTextField(date, { date = filterDateInput(it) }, S.date, Modifier.width(150.dp), keyboardType = KeyboardType.Number)
                QTextField(who, { who = it }, counterpartyLabel, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                QTextField(net, { net = filterDecimalInput(it) }, S.netAmount, Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                FilterChip(
                    selected = withVat, onClick = { withVat = !withVat },
                    label = { Text(S.withVat20) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = c.heroCyan.copy(alpha = 0.16f), selectedLabelColor = c.heroCyan,
                        containerColor = c.surfaceHigh, labelColor = c.textMuted,
                    ),
                )
            }
            Text("${S.totalWithVat}: ${formatMoneyUah(netD + vat)}  (${S.withVat20} ${formatMoneyUah(vat)})", color = c.textSecondary, style = MaterialTheme.typography.bodySmall)
            QPrimaryButton(S.post, onClick = { onSubmit(date.trim(), who.trim(), netD, vat) }, enabled = netD > 0 && who.isNotBlank(), modifier = Modifier.fillMaxWidth())
        }
    }
}

private fun formatMoneyUah(v: Double) = formatMoney(v, "UAH")
