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
import com.qlodi.cashpilot.ui.nav.CashpilotDestination
import com.qlodi.cashpilot.ui.theme.CashpilotColors

/** Dashboard — гроші, P&L-знімок, runway, задачі періоду (бриф 5.1). Числа — мок. */
@Composable
fun DashboardScreen(isCompact: Boolean) {
    val c = CashpilotColors
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Dashboard", "Огляд за червень 2026")
            Spacer(Modifier.weight(1f))
            QBadge("UA · UAH")
        }

        val kpis = listOf(
            Quad("Кошти на рахунках", "₴ 1 248 300", "+4.2%", c.positive),
            Quad("Дохід (MTD)", "₴ 420 000", "+12%", c.positive),
            Quad("Чистий прибуток", "₴ 96 500", "23% маржа", c.textSecondary),
            Quad("Runway", "8.4 міс", "при поточному burn", c.warning),
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

        // P&L mini + tasks
        if (isCompact) {
            PnlCard(); TasksCard()
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.weight(1.2f)) { PnlCard() }
                Box(Modifier.weight(1f)) { TasksCard() }
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
private fun PnlCard() {
    val c = CashpilotColors
    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("P&L знімок", color = c.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            PnlRow("Дохід", "₴ 420 000", c.positive)
            PnlRow("Витрати", "₴ 323 500", c.danger)
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
            PnlRow("Чистий прибуток", "₴ 96 500", c.textPrimary, bold = true)
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
private fun TasksCard() {
    val c = CashpilotColors
    QCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Задачі періоду", color = c.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            TaskRow(Icons.Filled.CalendarMonth, "Червень не закрито", "Trial balance збалансовано", c.warning)
            TaskRow(Icons.Filled.SwapVert, "7 банк-транзакцій нерознесено", "Перейти до reconciliation", c.heroCyan)
            TaskRow(Icons.Filled.Receipt, "2 прострочені інвойси", "₴ 54 000 у простроченні", c.danger)
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

/** Стилізований плейсхолдер для решти екранів. */
@Composable
fun PlaceholderScreen(dest: CashpilotDestination) {
    val c = CashpilotColors
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionTitle(dest.title, "Скелет екрана — функціонал додається за ТЗ")
        QCard(Modifier.fillMaxWidth(), padding = 24) {
            Text(hintFor(dest), color = c.textSecondary, fontSize = 14.sp)
        }
    }
}

private fun hintFor(dest: CashpilotDestination): String = when (dest) {
    CashpilotDestination.DASHBOARD -> ""
    CashpilotDestination.CHART_OF_ACCOUNTS -> "Дерево рахунків (Активи/Зобовʼязання/Капітал/Доходи/Витрати), сальдо, пошук."
    CashpilotDestination.JOURNAL -> "Журнал проводок Дт/Кт з індикатором балансу Σ Дт = Σ Кт. Лише сторно, без edit."
    CashpilotDestination.BANKING -> "Банк-рахунки, фіди, reconciliation (matching банк ↔ записи)."
    CashpilotDestination.INVOICES -> "Рахунки клієнтам (AR), статуси, aging, ПДВ-коди."
    CashpilotDestination.BILLS -> "Рахунки постачальників (AP), графік оплат, OCR-додавання."
    CashpilotDestination.TAXES -> "ПДВ-коди; UA — транзит 643/644; EU — OSS / reverse charge; податковий календар."
    CashpilotDestination.REPORTS -> "P&L, Balance Sheet, Cash Flow, Trial Balance, General Ledger."
    CashpilotDestination.PERIODS -> "Закриття періодів: Open → Soft-close → Locked."
    CashpilotDestination.SETTINGS -> "Юрисдикція (UA/EU/US), валюта, паки, ролі."
}
