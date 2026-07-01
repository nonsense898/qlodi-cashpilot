package com.qlodi.cashpilot.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.qlodi.cashpilot.ui.nav.CashpilotDestination

/** Мова інтерфейсу (EN/UA) — спільний патерн усіх продуктів Qlodi. */
enum class AppLanguage(val code: String, val label: String) {
    English("en", "EN"),
    Ukrainian("uk", "UA"),
}

/**
 * Рядковий бандл. `var`-поля + no-arg конструктор (як у frc — уникаємо ліміту
 * параметрів конструктора на Android). Будуємо два інстанси: EN та UA.
 */
class CashStrings {
    // common
    var appName = ""; var comingSoon = ""; var search = ""; var total = ""; var close = ""
    var cancel = ""; var open = ""; var dt = ""; var kt = ""
    // settings
    var settingsGroup = ""; var accountGroup = ""; var language = ""; var currencyLabel = ""
    var jurisdictionLabel = ""; var entityLabel = ""; var selectLanguage = ""
    var themeLabel = ""; var themeDark = ""; var themeLight = ""
    // nav
    var navDashboard = ""; var navAccounts = ""; var navJournal = ""; var navBanking = ""
    var navInvoices = ""; var navBills = ""; var navTaxes = ""; var navReports = ""
    var navPeriods = ""; var navSettings = ""; var navMore = ""; var navLogout = ""
    // auth
    var signIn = ""; var createAccountTitle = ""; var founderTag = ""; var email = ""; var password = ""
    var name = ""; var nameHint = ""; var createBtn = ""; var loginBtn = ""
    var haveAccount = ""; var noAccount = ""; var goLogin = ""; var goRegister = ""
    var wrongCredentials = ""; var noConnection = ""; var errUnbalanced = ""; var errPeriodLocked = ""; var errTooFewLines = ""
    // dashboard
    var overview = ""; var cashOnAccounts = ""; var revenue = ""; var expenses = ""; var netProfit = ""
    var profitWord = ""; var lossWord = ""; var entriesCountSuffix = ""; var pnlSnapshot = ""
    var statusTitle = ""; var entriesInJournal = ""; var sourceOfTruth = ""
    var tbBalancedShort = ""; var checkBalance = ""; var sumDtKt = ""; var bsBalanced = ""; var bsNotBalanced = ""; var aEqLE = ""
    // chart of accounts
    var accountsPlanSub = ""; var accountsCountWord = ""; var noAccounts = ""; var noAccountsSub = ""
    var assets = ""; var liabilities = ""; var equity = ""; var income = ""; var expense = ""
    // journal
    var entriesWord = ""; var newEntry = ""; var journalEmpty = ""; var journalEmptySub = ""
    var reversedBadge = ""; var reversalBadge = ""; var reverseAction = ""
    var reverseConfirmTitle = ""; var reverseConfirmMsg = ""; var reverseConfirm = ""
    var account = ""; var date = ""; var description = ""; var amount = ""; var addLine = ""
    var balancedBadge = ""; var unbalancedBadge = ""; var post = ""; var pickAccount = ""; var accountHint = ""
    // banking
    var cashPosition = ""; var totalOnAccounts = ""; var reconSoon = ""; var noCashAccounts = ""; var noCashAccountsSub = ""
    // taxes
    var taxesSub = ""; var vatToPayNet = ""; var vatEngineSoon = ""
    var vat643 = ""; var vat6411 = ""; var vat644 = ""
    // periods
    var periodsSub = ""; var noPeriods = ""; var noPeriodsSub = ""
    var pOpen = ""; var pSoftClosed = ""; var pLocked = ""; var lock = ""; var softClose = ""; var reopen = ""
    // invoices / bills
    var invoicesSub = ""; var client = ""; var invoiceBtn = ""; var noInvoices = ""; var createFirst = ""
    var billsSub = ""; var vendor = ""; var billBtn = ""; var noBills = ""
    var withVat20 = ""; var totalWithVat = ""; var netAmount = ""
    // reports
    var reportsSub = ""; var trialBalance = ""; var balanceSheet = ""; var accountCol = ""
    var totalRow = ""; var noData = ""; var addEntries = ""; var assetsTotal = ""; var liabEquityTotal = ""
    var cashFlow = ""; var cfOperating = ""; var cfInvesting = ""; var cfFinancing = ""
    var cfNetChange = ""; var cfOpening = ""; var cfClosing = ""
    var yearEndClose = ""; var yearEndCloseMsg = ""
    var pnlTab = ""; var pnlCogs = ""; var pnlGross = ""; var pnlAdmin = ""; var pnlSelling = ""
    var pnlOtherOpex = ""; var pnlOperating = ""; var pnlOtherIncome = ""; var pnlFinanceCost = ""; var pnlIncomeTax = ""
}

private fun en() = CashStrings().apply {
    appName = "CashPilot"; comingSoon = "Coming soon"; search = "Search"; total = "Total"; close = "Close"
    cancel = "Cancel"; open = "Open"; dt = "Dr"; kt = "Cr"
    settingsGroup = "SETTINGS"; accountGroup = "ACCOUNT"; language = "Language"; currencyLabel = "Currency"
    jurisdictionLabel = "Jurisdiction"; entityLabel = "Entity"; selectLanguage = "Choose language"
    themeLabel = "Theme"; themeDark = "Dark"; themeLight = "Light"
    navDashboard = "Dashboard"; navAccounts = "Chart of Accounts"; navJournal = "Journal"; navBanking = "Banking"
    navInvoices = "Invoices (AR)"; navBills = "Bills (AP)"; navTaxes = "Taxes / VAT"; navReports = "Reports"
    navPeriods = "Periods"; navSettings = "Settings"; navMore = "More"; navLogout = "Sign out"
    signIn = "Sign in"; createAccountTitle = "Create account"; founderTag = "accounting for founders"
    email = "Email"; password = "Password"; name = "Name"; nameHint = "Alex Kovalenko"
    createBtn = "Create"; loginBtn = "Sign in"; haveAccount = "Already have an account?"; noAccount = "No account?"
    goLogin = "Sign in"; goRegister = "Register"; wrongCredentials = "Wrong email or password"; noConnection = "No connection to server"
    errUnbalanced = "Σ Dr ≠ Σ Cr"; errPeriodLocked = "Period is locked"; errTooFewLines = "Entry needs ≥2 lines"
    overview = "Overview"; cashOnAccounts = "Cash on accounts"; revenue = "Revenue"; expenses = "Expenses"; netProfit = "Net profit"
    profitWord = "profit"; lossWord = "loss"; entriesCountSuffix = "entries"; pnlSnapshot = "P&L snapshot"
    statusTitle = "Status"; entriesInJournal = "entries in journal"; sourceOfTruth = "source of truth"
    tbBalancedShort = "Trial balance is balanced"; checkBalance = "Check balance"; sumDtKt = "Σ Dr = Σ Cr"
    bsBalanced = "Balance sheet ties"; bsNotBalanced = "Balance sheet off"; aEqLE = "A = L + E"
    accountsPlanSub = "Chart of accounts (UA GAAP)"; accountsCountWord = "accounts"; noAccounts = "No accounts"
    noAccountsSub = "Create an entity to seed the default chart of accounts."
    assets = "Assets"; liabilities = "Liabilities"; equity = "Equity"; income = "Income"; expense = "Expenses"
    entriesWord = "entries"; newEntry = "New entry"; journalEmpty = "Journal is empty"; journalEmptySub = "Post your first entry — Σ Dr = Σ Cr."
    reversedBadge = "reversed"; reversalBadge = "reversal"; reverseAction = "Reverse"
    reverseConfirmTitle = "Reverse entry?"; reverseConfirmMsg = "A mirror entry will be created; the original stays in the journal as REVERSED. Entries are immutable — reversal only."
    reverseConfirm = "Reverse"; account = "Account"; date = "Date"; description = "Description"; amount = "Amount"; addLine = "+ line"
    balancedBadge = "balanced"; unbalancedBadge = "Σ Dr ≠ Σ Cr"; post = "Post"; pickAccount = "Pick account"; accountHint = "Account…"
    cashPosition = "Cash position"; totalOnAccounts = "Total on accounts"; reconSoon = "Reconciliation (bank feeds) — next step."
    noCashAccounts = "No accounts"; noCashAccountsSub = "Cash accounts will appear from the chart of accounts."
    taxesSub = "VAT 20% · 643/644 transit"; vatToPayNet = "VAT to pay (net)"; vatEngineSoon = "UA-VAT-20 · 14 · 7 · 0 · exempt · NA — full engine next."
    vat643 = "643 Output VAT liability"; vat6411 = "6411 VAT settlements"; vat644 = "644 Input VAT credit"
    periodsSub = "Period close / lock"; noPeriods = "No periods"; noPeriodsSub = "They appear after the first entry."
    pOpen = "OPEN"; pSoftClosed = "SOFT-CLOSED"; pLocked = "LOCKED"; lock = "Lock"; softClose = "Soft-close"; reopen = "Reopen"
    invoicesSub = "Customer invoices · revenue + VAT"; client = "Client"; invoiceBtn = "Invoice"; noInvoices = "No invoices"
    createFirst = "Create the first via the button above."
    billsSub = "Vendor bills · expense + VAT"; vendor = "Vendor"; billBtn = "Bill"; noBills = "No bills"
    withVat20 = "VAT 20%"; totalWithVat = "Total"; netAmount = "Amount (net)"
    reportsSub = "Derived from the ledger"; trialBalance = "Trial Balance"; balanceSheet = "Balance Sheet"; accountCol = "Account"
    totalRow = "Total"; noData = "No data"; addEntries = "Add entries."; assetsTotal = "Total assets"; liabEquityTotal = "Total liabilities & equity"
    cashFlow = "Cash Flow"; cfOperating = "Operating"; cfInvesting = "Investing"; cfFinancing = "Financing"
    cfNetChange = "Net change"; cfOpening = "Opening cash"; cfClosing = "Closing cash"
    yearEndClose = "Year-end close"; yearEndCloseMsg = "Zeroes income & expenses into retained earnings (441). A closing entry is created."
    pnlTab = "P&L"; pnlCogs = "Cost of sales"; pnlGross = "Gross profit"; pnlAdmin = "Administrative"; pnlSelling = "Selling"
    pnlOtherOpex = "Other opex"; pnlOperating = "Operating profit"; pnlOtherIncome = "Other income"; pnlFinanceCost = "Finance cost"; pnlIncomeTax = "Income tax"
}

private fun uk() = CashStrings().apply {
    appName = "CashPilot"; comingSoon = "Скоро"; search = "Пошук"; total = "Разом"; close = "Закрити"
    cancel = "Скасувати"; open = "Відкрити"; dt = "Дт"; kt = "Кт"
    settingsGroup = "НАЛАШТУВАННЯ"; accountGroup = "АКАУНТ"; language = "Мова"; currencyLabel = "Валюта"
    jurisdictionLabel = "Юрисдикція"; entityLabel = "Компанія"; selectLanguage = "Обрати мову"
    themeLabel = "Тема"; themeDark = "Темна"; themeLight = "Світла"
    navDashboard = "Дашборд"; navAccounts = "План рахунків"; navJournal = "Журнал"; navBanking = "Банк"
    navInvoices = "Інвойси (AR)"; navBills = "Рахунки (AP)"; navTaxes = "Податки / ПДВ"; navReports = "Звіти"
    navPeriods = "Періоди"; navSettings = "Налаштування"; navMore = "Ще"; navLogout = "Вийти"
    signIn = "Вхід"; createAccountTitle = "Створити акаунт"; founderTag = "облік для фаундерів"
    email = "Email"; password = "Пароль"; name = "Імʼя"; nameHint = "Alex Kovalenko"
    createBtn = "Створити"; loginBtn = "Увійти"; haveAccount = "Вже є акаунт?"; noAccount = "Немає акаунта?"
    goLogin = "Увійти"; goRegister = "Реєстрація"; wrongCredentials = "Невірний email або пароль"; noConnection = "Немає звʼязку з сервером"
    errUnbalanced = "Σ Дт ≠ Σ Кт"; errPeriodLocked = "Період заблоковано"; errTooFewLines = "Потрібно ≥2 рядки"
    overview = "Огляд"; cashOnAccounts = "Кошти на рахунках"; revenue = "Дохід"; expenses = "Витрати"; netProfit = "Чистий прибуток"
    profitWord = "прибуток"; lossWord = "збиток"; entriesCountSuffix = "проводок"; pnlSnapshot = "P&L знімок"
    statusTitle = "Стан"; entriesInJournal = "проводок у журналі"; sourceOfTruth = "джерело істини"
    tbBalancedShort = "Trial balance збалансовано"; checkBalance = "Перевір баланс"; sumDtKt = "Σ Дт = Σ Кт"
    bsBalanced = "Баланс зведено"; bsNotBalanced = "Баланс не зведено"; aEqLE = "A = L + E"
    accountsPlanSub = "План рахунків НП(С)БО"; accountsCountWord = "рахунків"; noAccounts = "Немає рахунків"
    noAccountsSub = "Створіть entity, щоб отримати дефолтний план рахунків."
    assets = "Активи"; liabilities = "Зобовʼязання"; equity = "Капітал"; income = "Доходи"; expense = "Витрати"
    entriesWord = "проводок"; newEntry = "Нова проводка"; journalEmpty = "Журнал порожній"; journalEmptySub = "Створіть першу проводку — Σ Дт = Σ Кт."
    reversedBadge = "сторновано"; reversalBadge = "сторно"; reverseAction = "Сторнувати"
    reverseConfirmTitle = "Сторнувати проводку?"; reverseConfirmMsg = "Буде створено дзеркальну проводку; оригінал лишиться в журналі зі статусом REVERSED. Редагування неможливе — лише сторно."
    reverseConfirm = "Сторнувати"; account = "Рахунок"; date = "Дата"; description = "Опис"; amount = "Сума"; addLine = "+ рядок"
    balancedBadge = "збалансовано"; unbalancedBadge = "Σ Дт ≠ Σ Кт"; post = "Провести"; pickAccount = "Оберіть рахунок"; accountHint = "Рахунок…"
    cashPosition = "Грошова позиція"; totalOnAccounts = "Усього на рахунках"; reconSoon = "Reconciliation (фіди банку) — наступний крок."
    noCashAccounts = "Немає рахунків"; noCashAccountsSub = "Грошові рахунки зʼявляться з плану рахунків."
    taxesSub = "ПДВ 20% · транзит 643/644"; vatToPayNet = "ПДВ до сплати (нетто)"; vatEngineSoon = "UA-VAT-20 · 14 · 7 · 0 · exempt · NA — повний рушій далі."
    vat643 = "643 Податкові зобовʼязання (output)"; vat6411 = "6411 Розрахунки за ПДВ"; vat644 = "644 Податковий кредит (input)"
    periodsSub = "Закриття / лок періодів"; noPeriods = "Немає періодів"; noPeriodsSub = "Зʼявляться після першої проводки."
    pOpen = "Відкритий"; pSoftClosed = "Мʼяке закр."; pLocked = "Заблокований"; lock = "Заблокувати"; softClose = "Мʼяке закриття"; reopen = "Відкрити"
    invoicesSub = "Рахунки клієнтам · дохід + ПДВ"; client = "Клієнт"; invoiceBtn = "Інвойс"; noInvoices = "Немає інвойсів"
    createFirst = "Створіть перший через кнопку вгорі."
    billsSub = "Рахунки постачальників · витрата + ПДВ"; vendor = "Постачальник"; billBtn = "Рахунок"; noBills = "Немає рахунків"
    withVat20 = "ПДВ 20%"; totalWithVat = "Разом"; netAmount = "Сума (нетто)"
    reportsSub = "Деривуються з леджера"; trialBalance = "Оборотно-сальдова"; balanceSheet = "Баланс"; accountCol = "Рахунок"
    totalRow = "Разом"; noData = "Немає даних"; addEntries = "Додайте проводки."; assetsTotal = "Активи разом"; liabEquityTotal = "Пасиви разом"
    cashFlow = "Рух коштів"; cfOperating = "Операційна"; cfInvesting = "Інвестиційна"; cfFinancing = "Фінансова"
    cfNetChange = "Чиста зміна"; cfOpening = "Кошти на початок"; cfClosing = "Кошти на кінець"
    yearEndClose = "Закрити рік"; yearEndCloseMsg = "Обнуляє доходи й витрати в нерозподілений прибуток (441). Створюється closing-проводка."
    pnlTab = "P&L"; pnlCogs = "Собівартість"; pnlGross = "Валовий прибуток"; pnlAdmin = "Адміністративні"; pnlSelling = "Збут"
    pnlOtherOpex = "Інші операційні"; pnlOperating = "Операційний прибуток"; pnlOtherIncome = "Інші доходи"; pnlFinanceCost = "Фінвитрати"; pnlIncomeTax = "Податок на прибуток"
}

/** Локалізована назва розділу навігації. */
fun CashStrings.title(d: CashpilotDestination): String = when (d) {
    CashpilotDestination.DASHBOARD -> navDashboard
    CashpilotDestination.CHART_OF_ACCOUNTS -> navAccounts
    CashpilotDestination.JOURNAL -> navJournal
    CashpilotDestination.BANKING -> navBanking
    CashpilotDestination.INVOICES -> navInvoices
    CashpilotDestination.BILLS -> navBills
    CashpilotDestination.TAXES -> navTaxes
    CashpilotDestination.REPORTS -> navReports
    CashpilotDestination.PERIODS -> navPeriods
    CashpilotDestination.SETTINGS -> navSettings
}

/** Локалізує код помилки (з [AppState]); невідоме — повертає як є (raw-меседж беку). */
fun CashStrings.errorText(codeOrMsg: String?): String = when (codeOrMsg) {
    null -> ""
    "wrong_credentials" -> wrongCredentials
    "no_connection" -> noConnection
    "unbalanced" -> errUnbalanced
    "period_locked" -> errPeriodLocked
    "too_few_lines" -> errTooFewLines
    else -> codeOrMsg
}

/** Людська назва subtype рахунку (замість сирого FIXED_ASSET / CASH / AR …). */
fun subtypeLabel(sub: String?, uk: Boolean): String = when (sub) {
    null -> ""
    "FIXED_ASSET" -> if (uk) "Основні засоби" else "Fixed asset"
    "INTANGIBLE" -> if (uk) "Нематеріальні" else "Intangible"
    "CONTRA_ASSET" -> if (uk) "Контр-актив" else "Contra-asset"
    "CASH" -> if (uk) "Готівка" else "Cash"
    "BANK" -> if (uk) "Банк" else "Bank"
    "BANK_FX" -> if (uk) "Банк (валюта)" else "Bank (FX)"
    "AR" -> if (uk) "Дебіторка" else "Receivable"
    "AR_FX" -> if (uk) "Дебіторка (вал.)" else "Receivable (FX)"
    "PREPAYMENT" -> if (uk) "Аванси видані" else "Prepayment"
    "PREPAID_EXP" -> if (uk) "Витрати майбутніх" else "Prepaid expense"
    "VAT_INPUT_TRANSIT" -> if (uk) "ПДВ кредит (транзит)" else "VAT input (transit)"
    "SHARE_CAPITAL" -> if (uk) "Статутний капітал" else "Share capital"
    "RETAINED_EARNINGS" -> if (uk) "Нерозподілений приб." else "Retained earnings"
    "CONTRA_EQUITY" -> if (uk) "Контр-капітал" else "Contra-equity"
    "LOAN" -> if (uk) "Кредит" else "Loan"
    "AP" -> if (uk) "Кредиторка" else "Payable"
    "AP_FX" -> if (uk) "Кредиторка (вал.)" else "Payable (FX)"
    "VAT_PAYABLE" -> if (uk) "ПДВ до сплати" else "VAT payable"
    "TAX_PROFIT" -> if (uk) "Податок на прибуток" else "Profit tax"
    "TAX_PIT" -> if (uk) "ПДФО" else "PIT"
    "TAX_MIL" -> if (uk) "Військовий збір" else "Military levy"
    "VAT_OUTPUT_TRANSIT" -> if (uk) "ПДВ зобовʼяз. (транзит)" else "VAT output (transit)"
    "PAYROLL_SSC" -> if (uk) "ЄСВ" else "Social contrib."
    "PAYROLL_WAGES" -> if (uk) "Зарплата" else "Wages"
    "DEFERRED_REV" -> if (uk) "Доходи майбутніх" else "Deferred revenue"
    "OTHER_PAYABLE" -> if (uk) "Інші кредитори" else "Other payable"
    "REVENUE" -> if (uk) "Дохід" else "Revenue"
    "CONTRA_REVENUE" -> if (uk) "Вирахування з доходу" else "Contra-revenue"
    "FX_GAIN" -> if (uk) "Курсовий дохід" else "FX gain"
    "OTHER_INCOME" -> if (uk) "Інші доходи" else "Other income"
    "PNL_SUMMARY" -> if (uk) "Фінрезультат" else "P&L summary"
    "COGS" -> if (uk) "Собівартість" else "COGS"
    "ADMIN" -> if (uk) "Адмінвитрати" else "Admin"
    "SALES_MARKETING" -> if (uk) "Збут" else "Sales & mktg"
    "FX_LOSS" -> if (uk) "Курсові втрати" else "FX loss"
    "FINANCE_COST" -> if (uk) "Фінвитрати" else "Finance cost"
    "INCOME_TAX" -> if (uk) "Податок на прибуток" else "Income tax"
    else -> sub
}

/**
 * Локалізована назва рахунку за кодом НП(С)БО. Бекенд сідить назви українською —
 * тут мапимо по коду в обидві мови. Невідомий код (кастомний рахунок) → fallback.
 */
fun accountName(code: String, fallback: String, uk: Boolean): String {
    if (uk) return fallback  // бекенд уже українською
    return EN_ACCOUNT_NAMES[code] ?: fallback
}

private val EN_ACCOUNT_NAMES: Map<String, String> = mapOf(
    "10" to "Fixed assets", "127" to "Intangible assets (SW/IP)", "131" to "Accumulated depreciation",
    "301" to "Cash on hand", "311" to "Bank account (local)", "312" to "Bank account (FX)",
    "361" to "Accounts receivable", "362" to "Accounts receivable (FX)", "371" to "Advances paid",
    "39" to "Prepaid expenses", "644" to "Input VAT (transit)",
    "40" to "Share capital", "441" to "Retained earnings", "442" to "Accumulated losses", "46" to "Unpaid capital",
    "601" to "Short-term bank loans", "631" to "Accounts payable", "632" to "Accounts payable (FX)",
    "6411" to "VAT settlements", "6412" to "Profit tax settlements", "6413" to "PIT settlements", "6414" to "Military levy settlements",
    "643" to "Output VAT (transit)", "651" to "Social contributions (ESV)", "661" to "Payroll settlements",
    "681" to "Advances received", "685" to "Other payables", "69" to "Deferred income",
    "703" to "Revenue (services)", "704" to "Revenue deductions", "714" to "FX gain", "719" to "Other operating income",
    "791" to "Operating result", "90" to "Cost of sales", "92" to "Administrative expenses", "93" to "Selling expenses",
    "945" to "FX loss", "951" to "Interest expense", "98" to "Income tax",
)

/** Людський статус періоду. */
fun periodStatusLabel(status: String, uk: Boolean): String = when (status) {
    "OPEN" -> if (uk) "Відкритий" else "Open"
    "SOFT_CLOSED", "SOFT-CLOSED" -> if (uk) "Мʼяке закриття" else "Soft-closed"
    "LOCKED" -> if (uk) "Заблокований" else "Locked"
    else -> status
}

fun stringsFor(lang: AppLanguage): CashStrings = if (lang == AppLanguage.Ukrainian) uk() else en()

val LocalStrings = staticCompositionLocalOf { uk() }
val LocalLanguage = staticCompositionLocalOf { AppLanguage.Ukrainian }
