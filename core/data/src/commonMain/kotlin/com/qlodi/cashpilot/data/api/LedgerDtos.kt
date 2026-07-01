package com.qlodi.cashpilot.data.api

import kotlinx.serialization.Serializable

/* enums — дзеркалять бекенд (app.qlodi.model) */
enum class AccountType { ASSET, LIABILITY, EQUITY, INCOME, EXPENSE }
enum class NormalBalance { DEBIT, CREDIT }
enum class Direction { DEBIT, CREDIT }
enum class EntrySource { MANUAL, AR, AP, BANK, PAYROLL, FX, CLOSING, REVERSAL, OPENING }
enum class EntryStatus { POSTED, REVERSED }
enum class PeriodStatus { OPEN, SOFT_CLOSED, LOCKED }

@Serializable
data class PeriodView(val id: String, val periodStart: String, val periodEnd: String, val status: PeriodStatus)

@Serializable
data class AuthCredentials(val email: String, val password: String, val name: String? = null)

@Serializable
data class UserSession(
    val uid: String, val email: String,
    val idToken: String, val refreshToken: String,
)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class CreateEntityRequest(
    val name: String, val jurisdiction: String = "UA",
    val functionalCurrency: String = "UAH", val fiscalYearStartMonth: Int = 1,
)

@Serializable
data class EntityView(
    val id: String, val name: String, val jurisdiction: String,
    val functionalCurrency: String, val fiscalYearStartMonth: Int, val createdAt: String,
)

@Serializable
data class AccountView(
    val id: String, val code: String, val name: String, val type: AccountType,
    val subtype: String? = null, val parentId: String? = null,
    val normalBalance: NormalBalance, val currency: String? = null,
    val reportLine: String? = null, val isActive: Boolean = true,
)

@Serializable
data class JournalLineRequest(
    val accountId: String, val direction: Direction, val amount: String, val memo: String? = null,
)

@Serializable
data class PostEntryRequest(
    val entryDate: String, val currency: String = "UAH", val description: String? = null,
    val source: EntrySource = EntrySource.MANUAL, val idempotencyKey: String? = null,
    val lines: List<JournalLineRequest> = emptyList(),
)

@Serializable
data class JournalLineView(
    val lineNo: Int, val accountId: String, val accountCode: String, val accountName: String,
    val direction: Direction, val amountTxn: String, val amountFunc: String,
    val fxRate: String, val memo: String? = null,
)

@Serializable
data class JournalEntryView(
    val id: String, val entityId: String, val entryDate: String, val currency: String,
    val description: String? = null, val source: EntrySource, val status: EntryStatus,
    val reversalOf: String? = null, val postedAt: String, val lines: List<JournalLineView> = emptyList(),
)

@Serializable
data class TrialBalanceRow(
    val accountId: String, val code: String, val name: String,
    val debit: String, val credit: String, val balance: String,
)

@Serializable
data class TrialBalanceView(
    val asOf: String, val rows: List<TrialBalanceRow>,
    val totalDebit: String, val totalCredit: String, val balanced: Boolean,
)

@Serializable
data class CashFlowView(
    val from: String, val to: String,
    val operating: String, val investing: String, val financing: String,
    val netChange: String, val openingCash: String, val closingCash: String, val reconciles: Boolean,
)

@Serializable
data class BalanceSheetLine(val code: String, val name: String, val amount: String)

@Serializable
data class BalanceSheetView(
    val asOf: String,
    val assets: List<BalanceSheetLine>, val liabilities: List<BalanceSheetLine>, val equity: List<BalanceSheetLine>,
    val totalAssets: String, val totalLiabilitiesEquity: String, val balanced: Boolean,
)
