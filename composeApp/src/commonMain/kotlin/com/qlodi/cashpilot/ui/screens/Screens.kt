package com.qlodi.cashpilot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qlodi.cashpilot.ui.components.NumberText
import com.qlodi.cashpilot.ui.components.QBadge
import com.qlodi.cashpilot.ui.components.QCard
import com.qlodi.cashpilot.ui.components.SectionTitle
import com.qlodi.cashpilot.AppState
import com.qlodi.cashpilot.data.api.AccountType
import com.qlodi.cashpilot.data.api.Direction
import com.qlodi.cashpilot.data.api.EntryStatus
import androidx.compose.material.icons.filled.Settings
import com.qlodi.cashpilot.ui.i18n.LocalStrings
import com.qlodi.cashpilot.ui.i18n.title
import com.qlodi.cashpilot.ui.nav.CashpilotDestination
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.theme.Spacing
import com.qlodi.cashpilot.ui.util.formatMoney

/** Dashboard — гроші, P&L-знімок, runway, задачі періоду (бриф 5.1). Числа — мок. */
@Composable
fun DashboardScreen(state: AppState, isCompact: Boolean) {
    val c = CashpilotColors
    val S = LocalStrings.current
    fun amt(s: String) = s.toDoubleOrNull() ?: 0.0
    val posted = state.entries.filter { it.status == EntryStatus.POSTED }
    val accType = state.accounts.associate { it.id to it.type }
    val accSub = state.accounts.associate { it.id to it.subtype }
    var revenue = 0.0; var expense = 0.0; var cash = 0.0
    posted.forEach { e ->
        e.lines.forEach { ln ->
            val a = amt(ln.amountFunc); val dr = ln.direction == Direction.DEBIT
            when (accType[ln.accountId]) {
                AccountType.INCOME -> revenue += if (dr) -a else a
                AccountType.EXPENSE -> expense += if (dr) a else -a
                else -> {}
            }
            if (accSub[ln.accountId] in setOf("CASH", "BANK", "BANK_FX")) cash += if (dr) a else -a
        }
    }
    val profit = revenue - expense

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(S.navDashboard, state.entity?.name ?: "—")
            Spacer(Modifier.weight(1f))
            QBadge(state.entity?.let { "${it.jurisdiction} · ${it.functionalCurrency}" } ?: "—")
        }

        val kpis = listOf(
            Quad(S.cashOnAccounts, formatMoney(cash, "UAH"), "${posted.size} ${S.entriesCountSuffix}", c.textSecondary),
            Quad(S.revenue, formatMoney(revenue, "UAH"), S.total, c.positive),
            Quad(S.expenses, formatMoney(expense, "UAH"), S.total, c.danger),
            Quad(S.netProfit, formatMoney(profit, "UAH"), if (profit >= 0) S.profitWord else S.lossWord, if (profit >= 0) c.positive else c.danger),
        )
        if (isCompact) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                kpis.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { KpiCard(it, Modifier.weight(1f)) }
                    }
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                kpis.forEach { KpiCard(it, Modifier.weight(1f)) }
            }
        }

        // P&L mini + стан
        if (isCompact) {
            PnlCard(revenue, expense, profit); TasksCard(state)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.weight(1.2f)) { PnlCard(revenue, expense, profit) }
                Box(Modifier.weight(1f)) { TasksCard(state) }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

private data class Quad(val label: String, val value: String, val delta: String, val deltaColor: Color)

@Composable
private fun KpiCard(q: Quad, modifier: Modifier = Modifier) {
    val c = CashpilotColors
    QCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(q.label, color = c.textMuted, fontSize = 12.5.sp)
            NumberText(q.value, size = 22)
            Text(q.delta, color = q.deltaColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PnlCard(revenue: Double, expense: Double, profit: Double) {
    val c = CashpilotColors
    val S = LocalStrings.current
    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(S.pnlSnapshot, color = c.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            PnlRow(S.revenue, formatMoney(revenue, "UAH"), c.positive)
            PnlRow(S.expenses, formatMoney(expense, "UAH"), c.danger)
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
            PnlRow(S.netProfit, formatMoney(profit, "UAH"), if (profit >= 0) c.positive else c.danger, bold = true)
        }
    }
}

@Composable
private fun PnlRow(label: String, value: String, color: Color, bold: Boolean = false) {
    val c = CashpilotColors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = if (bold) c.textPrimary else c.textSecondary,
            fontSize = 13.sp, fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal)
        Spacer(Modifier.weight(1f))
        NumberText(value, color = color, size = 14, weight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun TasksCard(state: AppState) {
    val c = CashpilotColors
    val S = LocalStrings.current
    val tbOk = state.trialBalance?.balanced == true
    val bsOk = state.balanceSheet?.balanced == true
    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(S.statusTitle, color = c.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            TaskRow(Icons.Filled.SwapVert, "${state.entries.size} ${S.entriesInJournal}", S.sourceOfTruth, c.heroCyan)
            TaskRow(Icons.Filled.CalendarMonth, if (tbOk) S.tbBalancedShort else S.checkBalance, S.sumDtKt, if (tbOk) c.positive else c.warning)
            TaskRow(Icons.Filled.Receipt, if (bsOk) S.bsBalanced else S.bsNotBalanced, S.aEqLE, if (bsOk) c.positive else c.warning)
        }
    }
}

@Composable
private fun TaskRow(icon: ImageVector, title: String, sub: String, tint: Color) {
    val c = CashpilotColors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = c.textPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
            Text(sub, color = c.textMuted, fontSize = 12.sp)
        }
    }
}

/** Плейсхолдер екрана (Settings) — преміум empty-state. */
@Composable
fun PlaceholderScreen(dest: CashpilotDestination) {
    val S = LocalStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        SectionTitle(S.title(dest), S.comingSoon)
        com.qlodi.cashpilot.ui.components.EmptyState(Icons.Filled.Settings, S.title(dest), S.comingSoon)
    }
}
