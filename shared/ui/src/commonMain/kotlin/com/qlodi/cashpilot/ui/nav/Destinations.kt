package com.qlodi.cashpilot.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Ліва навігація CashPilot (desktop-first) — «Бриф для Claude Design», розд. 4.
 */
enum class CashpilotDestination(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard),
    CHART_OF_ACCOUNTS("Chart of Accounts", Icons.Filled.AccountTree),
    JOURNAL("Journal", Icons.Filled.SwapVert),
    BANKING("Banking", Icons.Filled.AccountBalance),
    INVOICES("Invoices (AR)", Icons.Filled.Receipt),
    BILLS("Bills (AP)", Icons.Filled.RequestQuote),
    TAXES("Taxes / VAT", Icons.Filled.AccountBalanceWallet),
    REPORTS("Reports", Icons.Filled.Assessment),
    PERIODS("Periods", Icons.Filled.CalendarMonth),
    SETTINGS("Settings", Icons.Filled.Settings),
}
