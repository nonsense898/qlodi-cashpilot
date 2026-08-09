package com.qlodi.cashpilot.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

/**
 * Єдина точка REST-шару CashPilot: auth + ledger endpoints під /v1/ledger.
 * Створюється раз; токен бере зі [SessionStore].
 */
class CashpilotApi(private val tokenProvider: TokenProvider = SessionStore) {
    private val client: HttpClient = ApiClient.create(tokenProvider)

    init {
        // Підключаємо refresh для SessionStore.
        SessionStore.refresher = { rt ->
            runCatching {
                val s: UserSession = client.post(ApiConfig.url("/auth/refresh")) {
                    setBody(RefreshRequest(rt))
                }.body()
                s.idToken to s.refreshToken
            }.getOrNull()
        }
    }

    /* ── Auth ── */
    suspend fun register(email: String, password: String, name: String?): ApiResult<UserSession> =
        apiCall { client.post(ApiConfig.url("/auth/register")) { setBody(AuthCredentials(email, password, name)) } }

    suspend fun login(email: String, password: String): ApiResult<UserSession> =
        apiCall { client.post(ApiConfig.url("/auth/login")) { setBody(AuthCredentials(email, password)) } }

    /* ── Entities ── */
    suspend fun listEntities(): ApiResult<List<EntityView>> =
        apiCall { client.get(ApiConfig.url("/ledger/entities")) }

    suspend fun createEntity(req: CreateEntityRequest): ApiResult<EntityView> =
        apiCall { client.post(ApiConfig.url("/ledger/entities")) { setBody(req) } }

    suspend fun updateEntity(eid: String, req: UpdateEntityRequest): ApiResult<EntityView> =
        apiCall { client.put(ApiConfig.url("/ledger/entities/$eid")) { setBody(req) } }

    /* ── Accounts ── */
    suspend fun listAccounts(eid: String): ApiResult<List<AccountView>> =
        apiCall { client.get(ApiConfig.url("/ledger/entities/$eid/accounts")) }

    /* ── Journal ── */
    suspend fun listEntries(eid: String): ApiResult<List<JournalEntryView>> =
        apiCall { client.get(ApiConfig.url("/ledger/entities/$eid/journal-entries")) }

    suspend fun postEntry(eid: String, req: PostEntryRequest): ApiResult<JournalEntryView> =
        apiCall { client.post(ApiConfig.url("/ledger/entities/$eid/journal-entries")) { setBody(req) } }

    suspend fun reverseEntry(eid: String, id: String): ApiResult<JournalEntryView> =
        apiCall { client.post(ApiConfig.url("/ledger/entities/$eid/journal-entries/$id/reverse")) }

    /** CSV-експорт журналу проводок (сирий текст із бекенду). */
    suspend fun exportJournalCsv(eid: String): ApiResult<String> =
        apiCall { client.get(ApiConfig.url("/ledger/entities/$eid/journal-entries/export")) }

    /* ── Periods ── */
    suspend fun listPeriods(eid: String): ApiResult<List<PeriodView>> =
        apiCall { client.get(ApiConfig.url("/ledger/entities/$eid/periods")) }

    suspend fun setPeriodStatus(eid: String, id: String, action: String): ApiResult<PeriodView> =
        apiCall { client.post(ApiConfig.url("/ledger/entities/$eid/periods/$id/$action")) }

    /* ── Reports ── */
    suspend fun trialBalance(eid: String, asOf: String): ApiResult<TrialBalanceView> =
        apiCall { client.get(ApiConfig.url("/ledger/entities/$eid/reports/trial-balance")) { parameter("asOf", asOf) } }

    suspend fun balanceSheet(eid: String, asOf: String): ApiResult<BalanceSheetView> =
        apiCall { client.get(ApiConfig.url("/ledger/entities/$eid/reports/balance-sheet")) { parameter("asOf", asOf) } }

    suspend fun cashFlow(eid: String, from: String, to: String): ApiResult<CashFlowView> =
        apiCall { client.get(ApiConfig.url("/ledger/entities/$eid/reports/cash-flow")) { parameter("from", from); parameter("to", to) } }

    suspend fun pnl(eid: String, from: String, to: String): ApiResult<PnlView> =
        apiCall { client.get(ApiConfig.url("/ledger/entities/$eid/reports/pnl")) { parameter("from", from); parameter("to", to) } }

    /** Консолідований P&L по всіх юрособах у презентаційній валюті. */
    suspend fun consolidatedPnl(presentation: String, from: String, to: String): ApiResult<ConsolidatedPnlView> =
        apiCall {
            client.get(ApiConfig.url("/ledger/consolidation/pnl")) {
                parameter("presentation", presentation); parameter("from", from); parameter("to", to)
            }
        }

    /* ── Banking / reconciliation ── */
    suspend fun importBank(eid: String, req: ImportBankRequest): ApiResult<ImportResult> =
        apiCall { client.post(ApiConfig.url("/ledger/entities/$eid/bank/import")) { setBody(req) } }

    suspend fun listBankTxns(eid: String, unmatchedOnly: Boolean = true): ApiResult<List<BankTxnView>> =
        apiCall { client.get(ApiConfig.url("/ledger/entities/$eid/bank/transactions")) { parameter("unmatchedOnly", unmatchedOnly.toString()) } }

    suspend fun reconcileBankTxn(eid: String, txnId: String, req: ReconcileRequest): ApiResult<JournalEntryView> =
        apiCall { client.post(ApiConfig.url("/ledger/entities/$eid/bank/transactions/$txnId/reconcile")) { setBody(req) } }

    suspend fun yearEndClose(eid: String): ApiResult<JournalEntryView> =
        apiCall { client.post(ApiConfig.url("/ledger/entities/$eid/periods/year-end-close")) }

    /* ── Податковий движок: ПДВ (Фаза 2) ── */
    suspend fun vatReport(eid: String, from: String, to: String): ApiResult<VatReportView> =
        apiCall { client.get(ApiConfig.url("/ledger/entities/$eid/reports/vat")) { parameter("from", from); parameter("to", to) } }

    /* ── UA Payroll (Фаза 2) ── */
    suspend fun listEmployees(eid: String): ApiResult<List<Employee>> =
        apiCall { client.get(ApiConfig.url("/ledger/entities/$eid/payroll/employees")) }

    suspend fun saveEmployee(eid: String, e: Employee): ApiResult<Employee> =
        apiCall { client.post(ApiConfig.url("/ledger/entities/$eid/payroll/employees")) { setBody(e) } }

    suspend fun deactivateEmployee(eid: String, id: String): ApiResult<Unit> =
        apiCall { client.delete(ApiConfig.url("/ledger/entities/$eid/payroll/employees/$id")) }

    suspend fun listPayrollRuns(eid: String): ApiResult<List<PayrollRun>> =
        apiCall { client.get(ApiConfig.url("/ledger/entities/$eid/payroll/runs")) }

    suspend fun runPayroll(eid: String, period: String): ApiResult<PayrollRun> =
        apiCall { client.post(ApiConfig.url("/ledger/entities/$eid/payroll/runs")) { setBody(RunPayrollRequest(period)) } }
}
