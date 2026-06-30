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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qlodi.cashpilot.AppState
import com.qlodi.cashpilot.data.api.*
import com.qlodi.cashpilot.ui.components.NumberText
import com.qlodi.cashpilot.ui.components.QBadge
import com.qlodi.cashpilot.ui.components.QCard
import com.qlodi.cashpilot.ui.components.SectionTitle
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.util.formatMoney
import kotlinx.coroutines.launch

private fun amt(s: String) = s.toDoubleOrNull() ?: 0.0

/** Сальдо рахунку (нормальна сторона) з проведених проводок — клієнтський розрахунок. */
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Banking", "Грошова позиція")
        QCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Усього на рахунках", color = c.textMuted, fontSize = 12.5.sp)
                NumberText(formatMoney(total, "UAH"), size = 26)
            }
        }
        QCard(Modifier.fillMaxWidth(), padding = 0) {
            Column {
                cash.forEachIndexed { i, a ->
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(a.code, color = c.heroCyan, fontSize = 13.sp, modifier = Modifier.width(48.dp))
                        Text(a.name, color = c.textPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        NumberText(formatMoney(balanceOf(state, a), "UAH"), size = 14)
                    }
                }
            }
        }
        Text("Reconciliation (фіди банку) — наступний крок.", color = c.textMuted, fontSize = 12.5.sp)
        Spacer(Modifier.height(40.dp))
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Taxes / VAT", "ПДВ 20% · транзит 643/644")
        QCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("ПДВ до сплати (нетто)", color = c.textMuted, fontSize = 12.5.sp)
                NumberText(formatMoney(net, "UAH"), size = 26, color = if (net > 0) c.warning else c.positive)
            }
        }
        QCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                VatRow("643 Податкові зобовʼязання (output)", outTransit)
                VatRow("6411 Розрахунки за ПДВ", payable)
                VatRow("644 Податковий кредит (input)", -inTransit)
            }
        }
        Text("UA-VAT-20 · 14 · 7 · 0 · exempt · NA — повний рушій далі.", color = c.textMuted, fontSize = 12.5.sp)
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun VatRow(label: String, value: Double) {
    val c = CashpilotColors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = c.textSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        NumberText(formatMoney(value, "UAH"), size = 13)
    }
}

/* ───────────── Periods ───────────── */

@Composable
fun PeriodsScreen(state: AppState) {
    val c = CashpilotColors
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Periods", "Закриття / лок періодів")
        if (state.periods.isEmpty()) {
            QCard(Modifier.fillMaxWidth(), padding = 24) { Text("Періоди зʼявляться після першої проводки.", color = c.textMuted, fontSize = 14.sp) }
        }
        state.periods.forEach { p ->
            QCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${p.periodStart} … ${p.periodEnd}", color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        val (txt, col) = when (p.status) {
                            PeriodStatus.OPEN -> "OPEN" to c.positive
                            PeriodStatus.SOFT_CLOSED -> "SOFT-CLOSED" to c.warning
                            PeriodStatus.LOCKED -> "LOCKED" to c.danger
                        }
                        QBadge(txt, col)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (p.status != PeriodStatus.OPEN) Chip("Відкрити") { scope.launch { state.setPeriod(p.id, "open") } }
                        if (p.status == PeriodStatus.OPEN) Chip("Soft-close") { scope.launch { state.setPeriod(p.id, "soft-close") } }
                        if (p.status != PeriodStatus.LOCKED) Chip("Lock", danger = true) { scope.launch { state.setPeriod(p.id, "lock") } }
                    }
                }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

/* ───────────── Invoices (AR) / Bills (AP) ───────────── */

@Composable
fun InvoicesScreen(state: AppState) = DocScreen(
    state, title = "Invoices (AR)", subtitle = "Рахунки клієнтам · дохід + ПДВ",
    counterpartyLabel = "Клієнт", touchSubtype = "AR", ctaLabel = "+ Інвойс",
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
    counterpartyLabel = "Постачальник", touchSubtype = "AP", ctaLabel = "+ Рахунок",
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
    touchSubtype: String, ctaLabel: String,
    onSubmit: suspend (date: String, who: String, net: Double, vat: Double) -> String?,
) {
    val c = CashpilotColors
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    val accIds = state.accounts.filter { it.subtype == touchSubtype }.map { it.id }.toSet()
    val docs = state.entries.filter { e -> e.lines.any { it.accountId in accIds } }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(title, subtitle)
            Spacer(Modifier.weight(1f))
            Chip(if (open) "Закрити" else ctaLabel) { open = !open }
        }
        if (open) DocForm(counterpartyLabel) { date, who, net, vat ->
            scope.launch { if (onSubmit(date, who, net, vat) == null) open = false }
        }
        docs.forEach { e ->
            QCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(e.description ?: "—", color = c.textPrimary, fontSize = 14.sp)
                        Text(e.entryDate, color = c.textMuted, fontSize = 12.sp)
                    }
                    val total = e.lines.filter { it.accountId in accIds }.sumOf { amt(it.amountFunc) }
                    NumberText(formatMoney(total, "UAH"), size = 14)
                }
            }
        }
        Spacer(Modifier.height(40.dp))
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
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniField("Дата", date, { date = it }, Modifier.width(130.dp))
                MiniField(counterpartyLabel, who, { who = it }, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                MiniField("Сума (нетто)", net, { net = it }, Modifier.weight(1f))
                Row(Modifier.clip(RoundedCornerShape(9.dp)).background(if (withVat) c.heroCyan.copy(alpha = 0.16f) else c.surfaceElevated)
                    .border(1.dp, c.border, RoundedCornerShape(9.dp)).clickable { withVat = !withVat }.padding(horizontal = 12.dp, vertical = 13.dp)) {
                    Text(if (withVat) "ПДВ 20% ✓" else "без ПДВ", color = if (withVat) c.heroCyan else c.textMuted, fontSize = 13.sp)
                }
            }
            Text("Разом: ${formatMoney(netD + vat, "UAH")}  (ПДВ ${formatMoney(vat, "UAH")})", color = c.textSecondary, fontSize = 12.5.sp)
            Chip("Провести", enabled = netD > 0 && who.isNotBlank()) { onSubmit(date.trim(), who.trim(), netD, vat) }
        }
    }
}

/* ───────────── small shared ───────────── */

@Composable
private fun Chip(text: String, enabled: Boolean = true, danger: Boolean = false, onClick: () -> Unit) {
    val c = CashpilotColors
    val bg = when { !enabled -> c.surfaceElevated; danger -> c.danger; else -> c.heroCyan }
    val fg = when { !enabled -> c.textMuted; danger -> Color(0xFF2A0A0A); else -> c.onAccent }
    Box(Modifier.clip(RoundedCornerShape(10.dp)).background(bg).clickable(enabled = enabled, onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(text, color = fg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MiniField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val c = CashpilotColors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = c.textMuted, fontSize = 11.sp)
        BasicTextField(value = value, onValueChange = onChange, singleLine = true,
            textStyle = TextStyle(color = c.textPrimary, fontSize = 14.sp), cursorBrush = SolidColor(c.heroCyan),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(c.surfaceElevated)
                .border(1.dp, c.border, RoundedCornerShape(9.dp)).padding(horizontal = 12.dp, vertical = 12.dp))
    }
}
