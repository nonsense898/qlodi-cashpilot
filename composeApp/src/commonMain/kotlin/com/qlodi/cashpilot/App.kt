package com.qlodi.cashpilot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qlodi.cashpilot.ui.nav.CashpilotDestination
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.theme.CashpilotTheme

/**
 * Скелет Qlodi CashPilot — ліва навігація (desktop-first) + плейсхолдери екранів.
 * Дані/леджер/бекенд (api.qlodi.app) підключаються наступними кроками.
 */
@Composable
fun App() = CashpilotTheme {
    val c = CashpilotColors
    var current by remember { mutableStateOf(CashpilotDestination.DASHBOARD) }

    Row(Modifier.fillMaxSize().background(c.background)) {
        NavRail(current) { current = it }
        Box(Modifier.fillMaxSize().padding(28.dp)) {
            ScreenPlaceholder(current)
        }
    }
}

@Composable
private fun NavRail(current: CashpilotDestination, onSelect: (CashpilotDestination) -> Unit) {
    val c = CashpilotColors
    Column(
        Modifier
            .fillMaxHeight()
            .width(248.dp)
            .background(c.surface)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Brand + entity badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(c.heroCyan),
                contentAlignment = Alignment.Center,
            ) { Text("Q", color = c.onAccent, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("CashPilot", color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("UA · UAH", color = c.textMuted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(16.dp))

        CashpilotDestination.entries.forEach { dest ->
            NavItem(dest, selected = dest == current) { onSelect(dest) }
        }
    }
}

@Composable
private fun NavItem(dest: CashpilotDestination, selected: Boolean, onClick: () -> Unit) {
    val c = CashpilotColors
    val bg = if (selected) c.surfaceElevated else c.surface
    val tint = if (selected) c.heroCyan else c.textSecondary
    Row(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(dest.icon, contentDescription = dest.title, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            dest.title,
            color = if (selected) c.textPrimary else c.textSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun ScreenPlaceholder(dest: CashpilotDestination) {
    val c = CashpilotColors
    Column {
        Text(
            dest.title,
            style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
            color = c.textPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text("Скелет екрана — функціонал додається за ТЗ.", color = c.textMuted, fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(c.surface)
                .padding(24.dp),
        ) {
            Text(
                hintFor(dest),
                color = c.textSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            )
        }
    }
}

private fun hintFor(dest: CashpilotDestination): String = when (dest) {
    CashpilotDestination.DASHBOARD -> "Грошова позиція, P&L-знімок, runway, задачі періоду."
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
