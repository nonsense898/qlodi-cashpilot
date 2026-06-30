package com.qlodi.cashpilot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qlodi.cashpilot.ui.components.PillNavBar
import com.qlodi.cashpilot.ui.components.PillNavItem
import com.qlodi.cashpilot.ui.nav.CashpilotDestination
import com.qlodi.cashpilot.ui.screens.AuthScreen
import com.qlodi.cashpilot.ui.screens.ChartOfAccountsScreen
import com.qlodi.cashpilot.ui.screens.DashboardScreen
import com.qlodi.cashpilot.ui.screens.JournalScreen
import com.qlodi.cashpilot.ui.screens.PlaceholderScreen
import com.qlodi.cashpilot.ui.screens.ReportsScreen
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.theme.CashpilotTheme
import com.qlodi.cashpilot.ui.theme.LocalIsCompact

@Composable
fun App() = CashpilotTheme {
    val state = remember { AppState() }
    Box(Modifier.fillMaxSize().background(CashpilotColors.background)) {
        if (!state.loggedIn) {
            AuthScreen(state)
        } else {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val compact = maxWidth < 600.dp
                CompositionLocalProvider(LocalIsCompact provides compact) {
                    if (compact) MobileShell(state) else DesktopShell(state)
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(dest: CashpilotDestination, state: AppState, isCompact: Boolean) = when (dest) {
    CashpilotDestination.DASHBOARD -> DashboardScreen(state, isCompact)
    CashpilotDestination.CHART_OF_ACCOUNTS -> ChartOfAccountsScreen(state)
    CashpilotDestination.JOURNAL -> JournalScreen(state)
    CashpilotDestination.REPORTS -> ReportsScreen(state)
    else -> PlaceholderScreen(dest)
}

/* ───────────────── Desktop ───────────────── */

@Composable
private fun DesktopShell(state: AppState) {
    var current by remember { mutableStateOf(CashpilotDestination.DASHBOARD) }
    Row(Modifier.fillMaxSize()) {
        NavRail(current, { current = it }, state)
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp)) {
            ScreenContent(current, state, isCompact = false)
        }
    }
}

@Composable
private fun NavRail(current: CashpilotDestination, onSelect: (CashpilotDestination) -> Unit, state: AppState) {
    val c = CashpilotColors
    Column(
        Modifier.fillMaxHeight().width(252.dp).background(c.surface).padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BrandHeader(state)
        Spacer(Modifier.height(16.dp))
        CashpilotDestination.entries.forEach { dest ->
            NavRailItem(dest.icon, dest.title, selected = dest == current) { onSelect(dest) }
        }
        Spacer(Modifier.height(8.dp))
        NavRailItem(Icons.Filled.Logout, "Вийти", selected = false) { state.logout() }
    }
}

@Composable
private fun NavRailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, selected: Boolean, onClick: () -> Unit) {
    val c = CashpilotColors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(if (selected) c.surfaceElevated else c.surface)
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, title, tint = if (selected) c.heroCyan else c.textSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, color = if (selected) c.textPrimary else c.textSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, fontSize = 14.sp)
    }
}

/* ───────────────── Mobile ───────────────── */

private val PrimaryTabs = listOf(
    CashpilotDestination.DASHBOARD to Icons.Filled.Dashboard,
    CashpilotDestination.JOURNAL to Icons.Filled.SwapVert,
    CashpilotDestination.BANKING to Icons.Filled.AccountBalance,
    CashpilotDestination.REPORTS to Icons.Filled.Receipt,
)

@Composable
private fun MobileShell(state: AppState) {
    var current by remember { mutableStateOf(CashpilotDestination.DASHBOARD) }
    var showMore by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { BrandHeader(state) }
            Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp)) {
                if (showMore) MoreList(current, state) { current = it; showMore = false }
                else ScreenContent(current, state, isCompact = true)
            }
        }
        PillNavBar(Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(start = 20.dp, end = 20.dp, bottom = 8.dp)) {
            PrimaryTabs.forEach { (dest, icon) ->
                PillNavItem(icon, dest.title.substringBefore(" "), active = !showMore && current == dest) {
                    current = dest; showMore = false
                }
            }
            PillNavItem(Icons.Filled.Apps, "More", active = showMore) { showMore = true }
        }
    }
}

@Composable
private fun MoreList(current: CashpilotDestination, state: AppState, onSelect: (CashpilotDestination) -> Unit) {
    val c = CashpilotColors
    val rest = CashpilotDestination.entries.filter { d -> PrimaryTabs.none { it.first == d } }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("More", color = c.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        rest.forEach { dest -> MoreRow(dest.icon, dest.title, dest == current) { onSelect(dest) } }
        MoreRow(Icons.Filled.Logout, "Вийти", false) { state.logout() }
    }
}

@Composable
private fun MoreRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, active: Boolean, onClick: () -> Unit) {
    val c = CashpilotColors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (active) c.surfaceElevated else c.surface)
            .clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, title, tint = if (active) c.heroCyan else c.textSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, color = c.textPrimary, fontSize = 15.sp)
    }
}

@Composable
private fun BrandHeader(state: AppState) {
    val c = CashpilotColors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(c.heroCyan), contentAlignment = Alignment.Center) {
            Text("Q", color = c.onAccent, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text("CashPilot", color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(state.entity?.let { "${it.jurisdiction} · ${it.functionalCurrency}" } ?: "—", color = c.textMuted, fontSize = 12.sp)
        }
    }
}
