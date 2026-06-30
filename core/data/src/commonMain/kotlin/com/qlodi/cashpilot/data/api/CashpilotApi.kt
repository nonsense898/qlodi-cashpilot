package com.qlodi.cashpilot.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
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
}
