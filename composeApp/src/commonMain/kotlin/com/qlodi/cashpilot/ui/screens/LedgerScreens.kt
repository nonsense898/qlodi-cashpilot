package com.qlodi.cashpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private fun money(s: String) = formatMoney(s.toDoubleOrNull() ?: 0.0, "UAH")

/* ───────────── Chart of Accounts ───────────── */

@Composable
fun ChartOfAccountsScreen(state: AppState) {
    val c = CashpilotColors
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Chart of Accounts", "План рахунків НП(С)БО · ${state.accounts.size} рахунків")
        AccountType.entries.forEach { type ->
            val group = state.accounts.filter { it.type == type }
            if (group.isEmpty()) return@forEach
            Text(typeLabel(type), color = c.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            QCard(Modifier.fillMaxWidth(), padding = 0) {
                Column {
                    group.forEachIndexed { i, a ->
                        if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(a.code, color = c.heroCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(56.dp))
                            Text(a.name, color = c.textPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            a.subtype?.let { QBadge(it, c.textMuted) }
                            Spacer(Modifier.width(8.dp))
                            Text(if (a.normalBalance == NormalBalance.DEBIT) "Дт" else "Кт",
                                color = c.textMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

/* ───────────── Journal ───────────── */

@Composable
fun JournalScreen(state: AppState) {
    val c = CashpilotColors
    val scope = rememberCoroutineScope()
    var showEditor by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Journal", "${state.entries.size} проводок")
            Spacer(Modifier.weight(1f))
            CtaButton(if (showEditor) "Закрити" else "+ Проводка") { showEditor = !showEditor }
        }

        if (showEditor) NewEntryEditor(state) { showEditor = false }

        state.entries.forEach { e ->
            QCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(e.entryDate, color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(8.dp))
                        Text(e.description ?: "—", color = c.textSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        if (e.status == EntryStatus.REVERSED) QBadge("сторновано", c.warning)
                        else if (e.source == EntrySource.REVERSAL) QBadge("сторно", c.textMuted)
                    }
                    e.lines.forEach { ln ->
                        Row(Modifier.fillMaxWidth().padding(start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${ln.accountCode}", color = c.heroCyan, fontSize = 12.sp, modifier = Modifier.width(48.dp))
                            Text(ln.accountName, color = c.textSecondary, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
                            val dr = ln.direction == Direction.DEBIT
                            NumberText(money(ln.amountFunc), color = if (dr) c.textPrimary else c.textMuted, size = 12)
                            Spacer(Modifier.width(6.dp))
                            Text(if (dr) "Дт" else "Кт", color = c.textMuted, fontSize = 11.sp, modifier = Modifier.width(20.dp))
                        }
                    }
                    if (e.status == EntryStatus.POSTED && e.source != EntrySource.REVERSAL) {
                        Text("Сторнувати", color = c.danger, fontSize = 12.sp,
                            modifier = Modifier.clickable { scope.launch { state.reverse(e.id) } })
                    }
                }
            }
        }
        Spacer(Modifier.height(40.dp))
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
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Нова проводка", color = c.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EditorField("Дата", date, { date = it }, Modifier.width(130.dp))
                EditorField("Опис", desc, { desc = it }, Modifier.weight(1f))
            }
            lines.forEachIndexed { i, l ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountPicker(state.accounts, l.account, Modifier.weight(1f)) { l.account = it }
                    DirToggle(l.direction) { l.direction = it }
                    EditorField("", l.amount, { l.amount = it }, Modifier.width(96.dp), "0.00")
                    if (lines.size > 2) Text("✕", color = c.textMuted, fontSize = 14.sp,
                        modifier = Modifier.clickable { lines.removeAt(i) })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("+ рядок", color = c.heroCyan, fontSize = 13.sp,
                    modifier = Modifier.clickable { lines.add(LineDraft(null, Direction.DEBIT, "")) })
                Spacer(Modifier.weight(1f))
                // Індикатор балансу Σ Дт = Σ Кт
                Text("Дт ${formatMoney(dr, "UAH")}  ·  Кт ${formatMoney(cr, "UAH")}",
                    color = if (balanced) c.positive else c.warning, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                QBadge(if (balanced) "збалансовано" else "Σ Дт ≠ Σ Кт", if (balanced) c.positive else c.warning)
            }
            err?.let { Text(it, color = c.danger, fontSize = 13.sp) }
            CtaButton(if (posting) "…" else "Провести", enabled = balanced && !posting) {
                scope.launch {
                    posting = true; err = null
                    val req = PostEntryRequest(
                        entryDate = date.trim(), description = desc.trim().ifBlank { null },
                        lines = lines.mapNotNull { l ->
                            val acc = l.account ?: return@mapNotNull null
                            JournalLineRequest(acc.id, l.direction, l.amount.trim())
                        },
                    )
                    err = state.post(req)
                    posting = false
                    if (err == null) onDone()
                }
            }
        }
    }
}

@Composable
private fun AccountPicker(accounts: List<AccountView>, selected: AccountView?, modifier: Modifier = Modifier, onPick: (AccountView) -> Unit) {
    val c = CashpilotColors
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(c.surfaceElevated)
                .border(1.dp, c.border, RoundedCornerShape(9.dp)).clickable { open = true }
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(selected?.let { "${it.code} ${it.name}" } ?: "Рахунок…",
                color = if (selected != null) c.textPrimary else c.textMuted, fontSize = 13.sp, maxLines = 1)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false },
            modifier = Modifier.background(c.surface).heightIn(max = 320.dp)) {
            accounts.forEach { a ->
                DropdownMenuItem(
                    text = { Text("${a.code}  ${a.name}", color = c.textPrimary, fontSize = 13.sp) },
                    onClick = { onPick(a); open = false },
                )
            }
        }
    }
}

@Composable
private fun DirToggle(dir: Direction, onChange: (Direction) -> Unit) {
    val c = CashpilotColors
    Row(
        Modifier.clip(RoundedCornerShape(9.dp)).background(c.surfaceElevated).border(1.dp, c.border, RoundedCornerShape(9.dp)),
    ) {
        listOf(Direction.DEBIT to "Дт", Direction.CREDIT to "Кт").forEach { (d, label) ->
            val on = d == dir
            Box(
                Modifier.clip(RoundedCornerShape(9.dp)).background(if (on) c.heroCyan else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onChange(d) }.padding(horizontal = 12.dp, vertical = 11.dp),
            ) { Text(label, color = if (on) c.onAccent else c.textMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
        }
    }
}

/* ───────────── Reports ───────────── */

@Composable
fun ReportsScreen(state: AppState) {
    val c = CashpilotColors
    var tab by remember { mutableStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Reports", "Деривуються з леджера")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Trial Balance", "Balance Sheet").forEachIndexed { i, t ->
                val on = i == tab
                Box(Modifier.clip(RoundedCornerShape(999.dp)).background(if (on) c.accentDim else c.surface)
                    .border(1.dp, if (on) c.heroCyan else c.border, RoundedCornerShape(999.dp))
                    .clickable { tab = i }.padding(horizontal = 16.dp, vertical = 9.dp)) {
                    Text(t, color = if (on) c.heroCyan else c.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        if (tab == 0) TrialBalanceCard(state.trialBalance) else BalanceSheetCard(state.balanceSheet)
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun TrialBalanceCard(tb: TrialBalanceView?) {
    val c = CashpilotColors
    if (tb == null) { EmptyHint(); return }
    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            tb.rows.forEach { r ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(r.code, color = c.heroCyan, fontSize = 12.5.sp, modifier = Modifier.width(48.dp))
                    Text(r.name, color = c.textPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    NumberText(if (r.debit.toDoubleOrNull() ?: 0.0 > 0) money(r.debit) else "", size = 12, modifier = Modifier.width(90.dp))
                    NumberText(if (r.credit.toDoubleOrNull() ?: 0.0 > 0) money(r.credit) else "", size = 12, modifier = Modifier.width(90.dp))
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Разом", color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                NumberText(money(tb.totalDebit), size = 13, weight = FontWeight.Bold, modifier = Modifier.width(90.dp))
                NumberText(money(tb.totalCredit), size = 13, weight = FontWeight.Bold, modifier = Modifier.width(90.dp))
            }
            QBadge(if (tb.balanced) "Σ Дт = Σ Кт ✓" else "не збалансовано", if (tb.balanced) c.positive else c.danger)
        }
    }
}

@Composable
private fun BalanceSheetCard(bs: BalanceSheetView?) {
    val c = CashpilotColors
    if (bs == null) { EmptyHint(); return }
    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BsSection("Активи", bs.assets, bs.totalAssets)
            Spacer(Modifier.height(6.dp))
            BsSection("Зобовʼязання", bs.liabilities, null)
            BsSection("Капітал", bs.equity, null)
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
            Row {
                Text("Пасиви разом", color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                NumberText(money(bs.totalLiabilitiesEquity), size = 13, weight = FontWeight.Bold)
            }
            QBadge(if (bs.balanced) "A = L + E ✓" else "не збалансовано", if (bs.balanced) c.positive else c.danger)
        }
    }
}

@Composable
private fun BsSection(title: String, lines: List<BalanceSheetLine>, total: String?) {
    val c = CashpilotColors
    Text(title, color = c.textSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
    lines.forEach { l ->
        Row(Modifier.padding(start = 4.dp)) {
            Text("${l.code}  ${l.name}", color = c.textPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
            NumberText(money(l.amount), size = 12)
        }
    }
    if (total != null) Row {
        Text("Активи разом", color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        NumberText(money(total), size = 13, weight = FontWeight.Bold)
    }
}

/* ───────────── shared bits ───────────── */

@Composable
private fun EmptyHint() {
    val c = CashpilotColors
    QCard(Modifier.fillMaxWidth(), padding = 24) { Text("Немає даних — додайте проводки.", color = c.textMuted, fontSize = 14.sp) }
}

@Composable
private fun CtaButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val c = CashpilotColors
    Box(
        Modifier.clip(RoundedCornerShape(11.dp)).background(if (enabled) c.heroCyan else c.surfaceElevated)
            .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 18.dp, vertical = 11.dp),
    ) { Text(text, color = if (enabled) c.onAccent else c.textMuted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun EditorField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, placeholder: String = "") {
    val c = CashpilotColors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (label.isNotEmpty()) Text(label, color = c.textMuted, fontSize = 11.sp)
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(c.surfaceElevated)
            .border(1.dp, c.border, RoundedCornerShape(9.dp)).padding(horizontal = 12.dp, vertical = 11.dp)) {
            if (value.isEmpty() && placeholder.isNotEmpty()) Text(placeholder, color = c.textMuted, fontSize = 13.sp)
            BasicTextField(value = value, onValueChange = onChange, singleLine = true,
                textStyle = TextStyle(color = c.textPrimary, fontSize = 13.sp), cursorBrush = SolidColor(c.heroCyan),
                modifier = Modifier.fillMaxWidth())
        }
    }
}

private fun typeLabel(t: AccountType) = when (t) {
    AccountType.ASSET -> "Активи"; AccountType.LIABILITY -> "Зобовʼязання"
    AccountType.EQUITY -> "Капітал"; AccountType.INCOME -> "Доходи"; AccountType.EXPENSE -> "Витрати"
}
