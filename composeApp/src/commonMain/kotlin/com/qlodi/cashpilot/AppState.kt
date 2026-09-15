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

    var entities by mutableStateOf<List<EntityView>>(emptyList()); private set
    var entity by mutableStateOf<EntityView?>(null); private set
    var accounts by mutableStateOf<List<AccountView>>(emptyList()); private set
    var entries by mutableStateOf<List<JournalEntryView>>(emptyList()); private set
    var trialBalance by mutableStateOf<TrialBalanceView?>(null); private set
    var balanceSheet by mutableStateOf<BalanceSheetView?>(null); private set
    var cashFlow by mutableStateOf<CashFlowView?>(null); private set
    var pnl by mutableStateOf<PnlView?>(null); private set
    var periods by mutableStateOf<List<PeriodView>>(emptyList()); private set
    var bankTxns by mutableStateOf<List<BankTxnView>>(emptyList()); private set
    /** Clients and invoices shared with Qlodi Business. */
    var clients by mutableStateOf<List<ClientDto>>(emptyList()); private set
    var invoices by mutableStateOf<List<InvoiceDto>>(emptyList()); private set

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

    /** Поки перевіряємо демо-режим — не показуємо екран входу (без «блимання»). */
    var demoChecking by mutableStateOf(!SessionStore.isLoggedIn && ApiConfig.demoRequested); private set

    /** Demo Day: якщо в адмінці увімкнено демо — одразу входимо в demo-акаунт. Один раз на завантаження сторінки. */
    suspend fun tryDemo() {
        if (!demoChecking) return
        val s = api.demoSession().getOrNull()
        demoChecking = false
        if (s != null && !loggedIn) {
            SessionStore.set(s.idToken, s.refreshToken, s.uid, s.email)
            loggedIn = true
            bootstrap()
        }
    }

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
        entities = list
        val e = list.firstOrNull() ?: api.createEntity(CreateEntityRequest(name = "Моя компанія", jurisdiction = "UA")).getOrNull()
            ?.also { entities = listOf(it) }
        entity = e
        if (e != null) { reloadAccounts(); reloadEntries(); reloadReports(); reloadPeriods(); reloadBank(); reloadBilling() }
        busy = false
    }

    /** Перемкнути активну компанію (кілька юросіб в одному акаунті). */
    suspend fun selectEntity(id: String) {
        if (entity?.id == id) return
        entity = entities.firstOrNull { it.id == id } ?: return
        busy = true
        reloadAccounts(); reloadEntries(); reloadReports(); reloadPeriods(); reloadBank(); reloadBilling()
        busy = false
    }

    /** Створити нову компанію з обраною валютою; одразу робить її активною. null при успіху. */
    suspend fun createCompany(name: String, functionalCurrency: String, jurisdiction: String): String? {
        val req = CreateEntityRequest(name = name.trim(), jurisdiction = jurisdiction, functionalCurrency = functionalCurrency)
        return when (val r = api.createEntity(req)) {
            is ApiResult.Ok -> {
                entities = entities + r.value
                selectEntity(r.value.id)
                null
            }
            is ApiResult.Err -> friendly(r.error)
        }
    }

    /** Перейменувати / змінити валюту активної компанії. null при успіху. */
    suspend fun updateCompany(name: String?, functionalCurrency: String?): String? {
        val eid = entity?.id ?: return "no_entity"
        return when (val r = api.updateEntity(eid, UpdateEntityRequest(name, functionalCurrency))) {
            is ApiResult.Ok -> {
                entity = r.value
                entities = entities.map { if (it.id == r.value.id) r.value else it }
                null
            }
            is ApiResult.Err -> friendly(r.error)
        }
    }

    suspend fun reloadBank() { entity?.let { bankTxns = api.listBankTxns(it.id, true).getOrNull().orEmpty() } }

    /** Імпорт CSV-виписки. null при успіху. */
    suspend fun importBank(bankAccountId: String, rows: List<BankTxnImport>): String? {
        val eid = entity?.id ?: return "no_entity"
        return when (val r = api.importBank(eid, ImportBankRequest(bankAccountId, rows))) {
            is ApiResult.Ok -> { reloadBank(); null }
            is ApiResult.Err -> friendly(r.error)
        }
    }

    /** Звірка банк-транзакції з контр-рахунком (створює проводку). null при успіху. */
    suspend fun reconcileBank(txnId: String, counterAccountId: String): String? {
        val eid = entity?.id ?: return "no_entity"
        return when (val r = api.reconcileBankTxn(eid, txnId, ReconcileRequest(counterAccountId))) {
            is ApiResult.Ok -> { reloadBank(); reloadEntries(); reloadReports(); null }
            is ApiResult.Err -> friendly(r.error)
        }
    }

    suspend fun reloadAccounts() { entity?.let { accounts = api.listAccounts(it.id).getOrNull().orEmpty() } }
    suspend fun reloadEntries() { entity?.let { entries = api.listEntries(it.id).getOrNull().orEmpty() } }
    suspend fun reloadPeriods() { entity?.let { periods = api.listPeriods(it.id).getOrNull().orEmpty() } }
    suspend fun reloadReports() {
        entity?.let {
            trialBalance = api.trialBalance(it.id, asOf).getOrNull()
            balanceSheet = api.balanceSheet(it.id, asOf).getOrNull()
            cashFlow = api.cashFlow(it.id, "1970-01-01", asOf).getOrNull()
            pnl = api.pnl(it.id, "1970-01-01", asOf).getOrNull()
        }
    }

    /** Закриття року. null при успіху, інакше код помилки. */
    suspend fun yearEndClose(): String? {
        val eid = entity?.id ?: return "no_entity"
        return when (val r = api.yearEndClose(eid)) {
            is ApiResult.Ok -> { reloadEntries(); reloadReports(); reloadPeriods(); null }
            is ApiResult.Err -> friendly(r.error)
        }
    }

    suspend fun setPeriod(id: String, action: String) {
        val eid = entity?.id ?: return
        if (api.setPeriodStatus(eid, id, action) is ApiResult.Ok) reloadPeriods()
    }

    suspend fun reloadBilling() {
        clients = api.listClients().getOrNull().orEmpty()
        invoices = api.listInvoices().getOrNull().orEmpty().sortedByDescending { it.issueDate }
    }

    /**
     * Invoices of the active company. Invoices without a company (made in Business) belong to the
     * first company — that is where the ledger bridge posts them.
     */
    fun invoicesForEntity(): List<InvoiceDto> {
        val e = entity ?: return emptyList()
        val first = entities.firstOrNull()?.id
        return invoices.filter { it.entityId == e.id || (it.entityId == null && e.id == first) }
    }

    /**
     * Create a shared invoice for the active company and issue it (ledger entry) or send it (+ e-mail).
     * Reuses a client with the same name, otherwise creates one. null on success, else an error code.
     */
    suspend fun createInvoice(
        clientName: String, email: String, issueDate: String, description: String,
        net: Double, vatRate: Double, send: Boolean,
    ): String? {
        val e = entity ?: return "no_entity"
        val name = clientName.trim()
        val existing = clients.firstOrNull { it.name.equals(name, ignoreCase = true) && it.status == "Active" }
        if (send && (existing?.billingEmail ?: email).isBlank()) return "email_required"
        val cl = existing ?: when (val r = api.createClient(
            ClientDto(id = newId(), name = name, billingEmail = email.trim(), currency = e.functionalCurrency),
        )) {
            is ApiResult.Ok -> r.value
            is ApiResult.Err -> return friendly(r.error)
        }
        val id = newId()
        val draft = InvoiceDto(
            id = id, clientId = cl.id, issueDate = issueDate, dueDate = plusDaysIso(issueDate, cl.paymentTermsDays),
            currency = e.functionalCurrency, source = "Manual", entityId = e.id,
            lines = listOf(InvoiceLineDto(id = newId(), invoiceId = id, description = description.ifBlank { "Services" },
                unitPrice = net, amount = net, taxRate = vatRate)),
        )
        val created = when (val r = api.createInvoice(draft)) {
            is ApiResult.Ok -> r.value
            is ApiResult.Err -> return friendly(r.error)
        }
        val done = if (send) api.sendInvoice(created.id) else api.issueInvoice(created.id)
        if (done is ApiResult.Err) return friendly(done.error)
        reloadBilling(); reloadEntries(); reloadReports(); reloadPeriods()
        return null
    }

    suspend fun issueInvoice(inv: InvoiceDto, send: Boolean): String? =
        afterInvoiceCall(if (send) api.sendInvoice(inv.id) else api.issueInvoice(inv.id))

    suspend fun markInvoicePaid(inv: InvoiceDto): String? =
        afterInvoiceCall(api.payInvoice(inv.id, (inv.total - inv.amountPaid).coerceAtLeast(0.0)))

    suspend fun revertInvoicePayment(inv: InvoiceDto): String? = afterInvoiceCall(api.unpayInvoice(inv.id))

    private suspend fun afterInvoiceCall(r: ApiResult<InvoiceDto>): String? = when (r) {
        is ApiResult.Ok -> { reloadBilling(); reloadEntries(); reloadReports(); null }
        is ApiResult.Err -> friendly(r.error)
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

    /** CSV-експорт журналу проводок. Повертає сирий текст або null (немає entity/помилка). */
    suspend fun exportJournalCsv(): String? = entity?.let { api.exportJournalCsv(it.id).getOrNull() }

    suspend fun reverse(id: String) {
        val eid = entity?.id ?: return
        if (api.reverseEntry(eid, id) is ApiResult.Ok) { reloadEntries(); reloadReports() }
    }

    fun logout() {
        SessionStore.clear()
        loggedIn = false; entity = null; accounts = emptyList(); entries = emptyList()
        trialBalance = null; balanceSheet = null
        clients = emptyList(); invoices = emptyList()
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

private fun newId(): String = buildString { repeat(24) { append("0123456789abcdef"[kotlin.random.Random.nextInt(16)]) } }

/** ISO date + n days (proleptic Gregorian, no timezone involved). */
private fun plusDaysIso(iso: String, days: Int): String {
    val p = iso.take(10).split("-")
    val y = p.getOrNull(0)?.toIntOrNull() ?: return iso
    val m = p.getOrNull(1)?.toIntOrNull() ?: return iso
    val d = p.getOrNull(2)?.toIntOrNull() ?: return iso
    val yy = if (m <= 2) y - 1 else y
    val era = (if (yy >= 0) yy else yy - 399) / 400
    val yoe = yy - era * 400
    val doy = (153 * ((m + 9) % 12) + 2) / 5 + d - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    val z = era * 146097L + doe - 719468 + days + 719468
    val era2 = (if (z >= 0) z else z - 146096) / 146097
    val doe2 = z - era2 * 146097
    val yoe2 = (doe2 - doe2 / 1460 + doe2 / 36524 - doe2 / 146096) / 365
    val doy2 = doe2 - (365 * yoe2 + yoe2 / 4 - yoe2 / 100)
    val mp = (5 * doy2 + 2) / 153
    val day = doy2 - (153 * mp + 2) / 5 + 1
    val month = if (mp < 10) mp + 3 else mp - 9
    val year = yoe2 + era2 * 400 + if (month <= 2) 1 else 0
    return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}
