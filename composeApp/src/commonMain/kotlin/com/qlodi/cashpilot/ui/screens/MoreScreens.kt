package com.qlodi.cashpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.qlodi.cashpilot.ui.i18n.errorText
import com.qlodi.cashpilot.ui.components.*
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.theme.Radii
import com.qlodi.cashpilot.ui.theme.Spacing
import com.qlodi.cashpilot.ui.util.parseBankCsv
import com.qlodi.cashpilot.ui.util.rememberCsvPickerState
import com.qlodi.cashpilot.ui.util.filterDateInput
import com.qlodi.cashpilot.ui.util.filterDecimalInput
import com.qlodi.cashpilot.ui.util.formatMoney
import com.qlodi.cashpilot.ui.util.parseAmount
import com.qlodi.cashpilot.ui.util.roundMoney
import com.qlodi.cashpilot.ui.util.moneyString
import com.qlodi.cashpilot.ui.util.todayIsoDate
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
    val scope = rememberCoroutineScope()
    val cash = state.accounts.filter { it.subtype in setOf("CASH", "BANK", "BANK_FX") }
    val total = cash.sumOf { balanceOf(state, it) }
    val bankAcc = state.accBySub("BANK") ?: cash.firstOrNull()
    var reconcileTxn by remember { mutableStateOf<BankTxnView?>(null) }
    var importMsg by remember { mutableStateOf<String?>(null) }
    var importFailed by remember { mutableStateOf(false) }
    val picker = rememberCsvPickerState { text ->
        if (text != null && bankAcc != null) {
            val parsed = parseBankCsv(text)
            val rows = parsed.rows.map { BankTxnImport(it.first, it.second, it.third) }
            if (rows.isEmpty()) {
                importFailed = true; importMsg = S.csvNoRows
            } else scope.launch {
                val err = state.importBank(bankAcc.id, rows)
                importFailed = err != null
                importMsg = err?.let { S.errorText(it) } ?: buildString {
                    append("${S.importedFmt}: ${rows.size}")
                    if (parsed.skipped > 0) append(" · ${S.csvSkippedFmt}: ${parsed.skipped}")
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(S.navBanking, S.cashPosition)
            Spacer(Modifier.weight(1f))
            if (bankAcc != null) QTonalButton(S.importStatement, { picker.pick() })
        }
        importMsg?.let {
            Text(it, color = if (importFailed) c.danger else c.positive, style = MaterialTheme.typography.bodySmall)
        }
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
        // Reconciliation — не звірені банк-транзакції (з CSV)
        if (state.bankTxns.isNotEmpty()) {
            Text("${S.unreconciled} · ${state.bankTxns.size}", color = c.textSecondary, style = MaterialTheme.typography.titleSmall)
            state.bankTxns.forEach { t ->
                QCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t.description ?: "—", color = c.textPrimary, style = MaterialTheme.typography.bodyLarge)
                            Text(t.txnDate, color = c.textMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        val inflow = (t.amount.toDoubleOrNull() ?: 0.0) >= 0
                        NumberText(formatMoneyUah(t.amount.toDoubleOrNull() ?: 0.0), color = if (inflow) c.positive else c.danger, size = 14)
                        Spacer(Modifier.width(Spacing.md))
                        QTonalButton(S.reconcileBtn, { reconcileTxn = t })
                    }
                }
            }
        } else {
            Text(S.noBankTxns, color = c.textMuted, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(Spacing.huge))
    }

    reconcileTxn?.let { t ->
        CounterAccountDialog(state.accounts, uk, S.chooseCounter, onDismiss = { reconcileTxn = null }) { acc ->
            reconcileTxn = null; scope.launch { state.reconcileBank(t.id, acc.id) }
        }
    }
}

/** Діалог вибору контр-рахунку для звірки. */
@Composable
private fun CounterAccountDialog(accounts: List<AccountView>, uk: Boolean, title: String, onDismiss: () -> Unit, onPick: (AccountView) -> Unit) {
    val c = CashpilotColors
    var query by remember { mutableStateOf("") }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(Radii.lg)).background(c.surface).border(1.dp, c.border, RoundedCornerShape(Radii.lg)).padding(Spacing.lg)) {
            Column(Modifier.heightIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(title, color = c.textPrimary, style = MaterialTheme.typography.titleMedium)
                QTextField(query, { query = it }, "")
                val filtered = accounts.filter { query.isBlank() || it.code.contains(query, true) || it.name.contains(query, true) || accountName(it.code, it.name, uk).contains(query, true) }
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(filtered, key = { it.id }) { a ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(Radii.sm)).clickable { onPick(a) }.padding(horizontal = Spacing.md, vertical = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                            Text(a.code, color = c.heroCyan, style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(52.dp))
                            Text(accountName(a.code, a.name, uk), color = c.textPrimary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

/* ───────────── Taxes / VAT ───────────── */

@Composable
fun TaxesScreen(state: AppState) {
    val c = CashpilotColors
    val S = LocalStrings.current
    // Серверний ПДВ-звіт (Фаза 2): обороти за поточний місяць + сальдо 6411/643/644.
    var report by remember { mutableStateOf<VatReportView?>(null) }
    LaunchedEffect(state.entity?.id, state.entries.size) {
        state.entity?.let { e ->
            val today = todayIsoDate()
            report = state.api.vatReport(e.id, today.take(7) + "-01", today).getOrNull()
        }
    }
    val r = report
    val payable = r?.balanceVatPayable?.toDoubleOrNull()
        ?: state.accBySub("VAT_PAYABLE")?.let { balanceOf(state, it) } ?: 0.0
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        SectionTitle(S.navTaxes, S.taxesSub)
        QCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(S.vatToPayNet, color = c.textMuted, style = MaterialTheme.typography.bodyMedium)
                NumberText(formatMoneyUah(payable), size = 28, color = if (payable > 0) c.warning else c.positive)
            }
        }
        if (r != null) {
            QCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Text("${r.from} → ${r.to}", color = c.textMuted, style = MaterialTheme.typography.labelMedium)
                    VatRow(S.vatPeriodOutput, r.outputVat.toDoubleOrNull() ?: 0.0)
                    VatRow(S.vatPeriodInput, -(r.inputVat.toDoubleOrNull() ?: 0.0))
                    VatRow(S.vatPeriodDue, r.netDue.toDoubleOrNull() ?: 0.0)
                }
            }
            QCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    VatRow(S.vat643, r.transitOutput643.toDoubleOrNull() ?: 0.0)
                    VatRow(S.vat6411, r.balanceVatPayable.toDoubleOrNull() ?: 0.0)
                    VatRow(S.vat644, r.transitInput644.toDoubleOrNull() ?: 0.0)
                }
            }
        }
        Text(S.vatDeclarationHint, color = c.textMuted, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(Spacing.huge))
    }
}

/* ───────────── UA Payroll (Фаза 2, doc_05) ───────────── */

@Composable
fun PayrollScreen(state: AppState) {
    val c = CashpilotColors
    val S = LocalStrings.current
    val scope = rememberCoroutineScope()
    var employees by remember { mutableStateOf<List<Employee>>(emptyList()) }
    var runs by remember { mutableStateOf<List<PayrollRun>>(emptyList()) }
    var adding by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf<String?>(null) }
    var empQuery by remember { mutableStateOf("") }

    suspend fun reload() {
        state.entity?.let { e ->
            employees = state.api.listEmployees(e.id).getOrNull().orEmpty().filter { it.active }
            runs = state.api.listPayrollRuns(e.id).getOrNull().orEmpty()
        }
    }
    LaunchedEffect(state.entity?.id) { reload() }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Payroll", S.payrollSub)
            Spacer(Modifier.weight(1f))
            QTonalButton(S.addEmployee, { adding = true })
        }
        msg?.let { Text(it, color = c.positive, style = MaterialTheme.typography.bodySmall) }

        // ── Працівники ──
        Text("${S.employeesTitle} · ${employees.size}", color = c.textSecondary, style = MaterialTheme.typography.titleSmall)
        if (employees.isEmpty()) Text(S.noEmployees, color = c.textMuted, style = MaterialTheme.typography.bodySmall)
        if (employees.size > 8) QTextField(empQuery, { empQuery = it }, S.search,
            trailingIcon = { Icon(Icons.Filled.Search, null, tint = c.textMuted) })
        val q = empQuery.trim()
        val shownEmp = if (q.isBlank()) employees else employees.filter {
            it.fullName.contains(q, true) || (it.taxId?.contains(q, true) == true)
        }
        if (employees.isNotEmpty() && shownEmp.isEmpty())
            Text(S.noResults, color = c.textMuted, style = MaterialTheme.typography.bodySmall)
        shownEmp.forEach { e ->
            QCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(e.fullName, color = c.textPrimary, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${formatMoneyUah(e.monthlySalary)}${if (e.diiaCity) " · Дія City" else ""}",
                            color = c.textMuted, style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    QTextLinkButton("✕", onClick = {
                        scope.launch { state.api.deactivateEmployee(state.entity!!.id, e.id); reload() }
                    })
                }
            }
        }
        if (adding) {
            var name by remember { mutableStateOf("") }
            var salary by remember { mutableStateOf("") }
            var diia by remember { mutableStateOf(false) }
            var fop by remember { mutableStateOf(false) }
            var esvRate by remember { mutableStateOf("") }
            QCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    QTextField(name, { name = it }, S.empName, Modifier.fillMaxWidth())
                    QTextField(salary, { salary = filterDecimalInput(it) }, S.empSalary, Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        FilterChip(
                            selected = diia, onClick = { diia = !diia; if (diia) fop = false }, label = { Text(S.empDiia) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = c.heroCyan.copy(alpha = 0.16f), selectedLabelColor = c.heroCyan,
                                containerColor = c.surfaceHigh, labelColor = c.textMuted,
                            ),
                        )
                        FilterChip(
                            selected = fop, onClick = { fop = !fop; if (fop) diia = false }, label = { Text(S.empFop) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = c.heroCyan.copy(alpha = 0.16f), selectedLabelColor = c.heroCyan,
                                containerColor = c.surfaceHigh, labelColor = c.textMuted,
                            ),
                        )
                    }
                    // ФОП сам платить податки — override ставки ЄСВ не застосовується.
                    if (!fop) QTextField(esvRate, { esvRate = filterDecimalInput(it) }, S.empEsvOverride, Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        QPrimaryButton(S.post, onClick = {
                            val sal = parseAmount(salary)
                            val esvOverride = esvRate.trim().takeIf { it.isNotBlank() && !fop }?.let { parseAmount(it) / 100.0 }
                            scope.launch {
                                state.api.saveEmployee(
                                    state.entity!!.id,
                                    Employee(
                                        id = randomEmpId(), fullName = name.trim(), monthlySalary = roundMoney(sal),
                                        employmentType = if (fop) "fop" else "main", diiaCity = diia,
                                        sscRateOverride = esvOverride,
                                    ),
                                )
                                adding = false; reload()
                            }
                        }, enabled = name.isNotBlank() && parseAmount(salary) > 0)
                        QTextLinkButton("Cancel", onClick = { adding = false })
                    }
                }
            }
        }

        // ── Нарахування ──
        if (employees.isNotEmpty()) {
            QPrimaryButton("${S.runPayrollBtn} · ${todayIsoDate().take(7)}", onClick = {
                scope.launch {
                    val r = state.api.runPayroll(state.entity!!.id, todayIsoDate().take(7))
                    msg = if (r.getOrNull() != null) S.payrollPosted else null
                    reload()
                    state.reloadEntries(); state.reloadReports()
                }
            }, modifier = Modifier.fillMaxWidth())
        }

        // ── Історія ──
        if (runs.isNotEmpty()) {
            Text(S.payrollRuns, color = c.textSecondary, style = MaterialTheme.typography.titleSmall)
            runs.forEach { r ->
                QCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Row {
                            Text(r.period, color = c.heroCyan, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.weight(1f))
                            Text(S.payrollPosted, color = c.positive, style = MaterialTheme.typography.labelMedium)
                        }
                        Text(
                            "${S.grossCol} ${r.totalGross} · ПДФО ${r.totalPit} · ВЗ ${r.totalMilitaryLevy} · ЄСВ ${r.totalSsc} · ${S.netCol} ${r.totalNet}",
                            color = c.textMuted, style = MaterialTheme.typography.bodySmall,
                        )
                        r.payslips.forEach { p ->
                            Text("  ${p.fullName}: ${S.netCol} ${p.net}", color = c.textSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        Text(S.payrollValidationNote, color = c.textMuted, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(Spacing.huge))
    }
}

private fun randomEmpId(): String = buildString {
    val hex = "0123456789abcdef"
    repeat(16) { append(hex[kotlin.random.Random.nextInt(16)]) }
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
    var confirmClose by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(S.navPeriods, S.periodsSub)
            Spacer(Modifier.weight(1f))
            if (state.periods.isNotEmpty()) QTonalButton(S.yearEndClose, { confirmClose = true })
        }
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

    if (confirmClose) {
        ConfirmDialog(
            title = S.yearEndClose, message = S.yearEndCloseMsg, confirmLabel = S.yearEndClose,
            onConfirm = { confirmClose = false; scope.launch { state.yearEndClose() } },
            onDismiss = { confirmClose = false },
        )
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
    // Реалізація (перша подія): ПДВ-зобов'язання одразу на 6411; 643 — лише аванси.
    val vatAcc = state.accBySub("VAT_PAYABLE") ?: state.accBySub("VAT_OUTPUT_TRANSIT")
    val total = net + vat
    val lines = buildList {
        add(JournalLineRequest(ar.id, Direction.DEBIT, moneyString(total)))
        add(JournalLineRequest(rev.id, Direction.CREDIT, moneyString(net)))
        if (vat > 0 && vatAcc != null) add(JournalLineRequest(vatAcc.id, Direction.CREDIT, moneyString(vat)))
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
    // Податковий кредит за зареєстрованою ПН — одразу на 6411; 644 — для непідтверджених.
    val vatAcc = state.accBySub("VAT_PAYABLE") ?: state.accBySub("VAT_INPUT_TRANSIT")
    val total = net + vat
    val lines = buildList {
        add(JournalLineRequest(exp.id, Direction.DEBIT, moneyString(net)))
        if (vat > 0 && vatAcc != null) add(JournalLineRequest(vatAcc.id, Direction.DEBIT, moneyString(vat)))
        add(JournalLineRequest(ap.id, Direction.CREDIT, moneyString(total)))
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
    var vatRate by remember { mutableStateOf(20) }   // 20/14/7/0 %, -1 = звільнено
    val netD = roundMoney(parseAmount(net))
    val vat = if (vatRate > 0) roundMoney(netD * vatRate / 100.0) else 0.0

    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                QTextField(date, { date = filterDateInput(it) }, S.date, Modifier.width(150.dp), keyboardType = KeyboardType.Number)
                QTextField(who, { who = it }, counterpartyLabel, Modifier.weight(1f))
            }
            QTextField(net, { net = filterDecimalInput(it) }, S.netAmount, Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
            Text(S.vatRate, color = c.textMuted, style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf(20, 14, 7, 0, -1).forEach { r ->
                    FilterChip(
                        selected = r == vatRate, onClick = { vatRate = r },
                        label = { Text(if (r == -1) S.vatExempt else "$r%") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = c.heroCyan.copy(alpha = 0.16f), selectedLabelColor = c.heroCyan,
                            containerColor = c.surfaceHigh, labelColor = c.textMuted,
                        ),
                    )
                }
            }
            Text("${S.totalWithVat}: ${formatMoneyUah(netD + vat)}  (VAT ${formatMoneyUah(vat)})", color = c.textSecondary, style = MaterialTheme.typography.bodySmall)
            QPrimaryButton(S.post, onClick = { onSubmit(date.trim(), who.trim(), netD, vat) }, enabled = netD > 0 && who.isNotBlank(), modifier = Modifier.fillMaxWidth())
        }
    }
}

private fun formatMoneyUah(v: Double) = formatMoney(v, "UAH")
