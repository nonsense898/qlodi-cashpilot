package com.qlodi.cashpilot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qlodi.cashpilot.data.api.*
import com.qlodi.cashpilot.ui.i18n.AppLanguage
import com.qlodi.cashpilot.ui.util.LocaleConfig

/**
 * Стан застосунку CashPilot: auth + поточний entity + дані леджера на живому API.
 * Методи suspend — UI запускає їх зі свого scope.
 */
class AppState {
    val api = CashpilotApi()

    var language by mutableStateOf(AppLanguage.Ukrainian); private set
    var loggedIn by mutableStateOf(SessionStore.isLoggedIn); private set
    var busy by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null)

    var entity by mutableStateOf<EntityView?>(null); private set
    var accounts by mutableStateOf<List<AccountView>>(emptyList()); private set
    var entries by mutableStateOf<List<JournalEntryView>>(emptyList()); private set
    var trialBalance by mutableStateOf<TrialBalanceView?>(null); private set
    var balanceSheet by mutableStateOf<BalanceSheetView?>(null); private set
    var periods by mutableStateOf<List<PeriodView>>(emptyList()); private set

    /** asOf для звітів — «усе» (включає всі проводки). */
    private val asOf = "2100-12-31"

    init {
        ApiConfig.onUnauthorized = { logout() }
        LocaleConfig.locale = language.code
    }

    fun setLanguage(lang: AppLanguage) {
        language = lang
        LocaleConfig.locale = lang.code
    }

    fun toggleLanguage() = setLanguage(if (language == AppLanguage.Ukrainian) AppLanguage.English else AppLanguage.Ukrainian)

    suspend fun login(email: String, password: String) = auth { api.login(email, password) }
    suspend fun register(email: String, password: String, name: String?) = auth { api.register(email, password, name) }

    private suspend fun auth(call: suspend () -> ApiResult<UserSession>) {
        busy = true; error = null
        when (val r = call()) {
            is ApiResult.Ok -> {
                val s = r.value
                SessionStore.set(s.idToken, s.refreshToken, s.uid, s.email)
                loggedIn = true
                bootstrap()
            }
            is ApiResult.Err -> error = friendly(r.error)
        }
        busy = false
    }

    /** Після логіну: гарантуємо entity (UA seed), вантажимо дані. */
    suspend fun bootstrap() {
        busy = true; error = null
        val list = api.listEntities().getOrNull().orEmpty()
        val e = list.firstOrNull() ?: api.createEntity(CreateEntityRequest(name = "Моя компанія", jurisdiction = "UA")).getOrNull()
        entity = e
        if (e != null) { reloadAccounts(); reloadEntries(); reloadReports(); reloadPeriods() }
        busy = false
    }

    suspend fun reloadAccounts() { entity?.let { accounts = api.listAccounts(it.id).getOrNull().orEmpty() } }
    suspend fun reloadEntries() { entity?.let { entries = api.listEntries(it.id).getOrNull().orEmpty() } }
    suspend fun reloadPeriods() { entity?.let { periods = api.listPeriods(it.id).getOrNull().orEmpty() } }
    suspend fun reloadReports() {
        entity?.let {
            trialBalance = api.trialBalance(it.id, asOf).getOrNull()
            balanceSheet = api.balanceSheet(it.id, asOf).getOrNull()
        }
    }

    suspend fun setPeriod(id: String, action: String) {
        val eid = entity?.id ?: return
        if (api.setPeriodStatus(eid, id, action) is ApiResult.Ok) reloadPeriods()
    }

    /** Перший активний рахунок із заданим subtype (для інвойсів/білів). */
    fun accBySub(sub: String): AccountView? = accounts.firstOrNull { it.subtype == sub && it.isActive }

    /** Провести проводку. Повертає null при успіху, інакше — текст помилки. */
    suspend fun post(req: PostEntryRequest): String? {
        val eid = entity?.id ?: return "Немає entity"
        return when (val r = api.postEntry(eid, req)) {
            is ApiResult.Ok -> { reloadEntries(); reloadReports(); reloadPeriods(); null }
            is ApiResult.Err -> friendly(r.error)
        }
    }

    suspend fun reverse(id: String) {
        val eid = entity?.id ?: return
        if (api.reverseEntry(eid, id) is ApiResult.Ok) { reloadEntries(); reloadReports() }
    }

    fun logout() {
        SessionStore.clear()
        loggedIn = false; entity = null; accounts = emptyList(); entries = emptyList()
        trialBalance = null; balanceSheet = null
    }

    // Повертаємо КОД помилки (UI локалізує через CashStrings.errorText); невідоме — raw-меседж.
    private fun friendly(e: ApiException): String {
        val code = e.code
        return when {
            e.status == 401 || code == "invalid_credentials" -> "wrong_credentials"
            code != null && code in setOf("unbalanced", "period_locked", "too_few_lines") -> code
            e.status == 0 -> "no_connection"
            else -> e.message
        }
    }
}
