package com.qlodi.cashpilot.domain.model

import kotlinx.serialization.Serializable

/**
 * Скелет доменної моделі леджера подвійного запису (Фаза 0).
 * Гроші — рядки decimal ("1234.56"), щоб не втрачати точність на клієнті;
 * усі обчислення/інваріант (Σ Дт = Σ Кт) виконує бекенд (ledger-core).
 *
 * Деталі: див. ТЗ «Фаза 0 — Леджер-ядро» та «Повне ТЗ CashPilot».
 */

/** Тип рахунку плану рахунків. */
enum class AccountType { ASSET, LIABILITY, EQUITY, INCOME, EXPENSE }

/** Юрисдикція entity — визначає Jurisdiction Pack (CoA, податки, payroll). */
enum class Jurisdiction { UA, EU, US }

/** Сторона проводки. */
enum class Direction { DEBIT, CREDIT }

/** Стан облікового періоду. */
enum class PeriodState { OPEN, SOFT_CLOSED, LOCKED }

/** Рахунок плану рахунків (Chart of Accounts), ієрархічний. */
@Serializable
data class Account(
    val id: String,
    val code: String,
    val name: String,
    val type: AccountType,
    val parentId: String? = null,
    val currency: String = "UAH",
    val active: Boolean = true,
)

/** Рядок проводки: рахунок + сторона + сума у валюті операції. */
@Serializable
data class JournalLine(
    val accountId: String,
    val direction: Direction,
    val amount: String,        // decimal як рядок, напр. "10000.00"
    val currency: String = "UAH",
    val memo: String? = null,
)

/** Проводка (journal entry): ≥2 рядки, Σ Дт = Σ Кт. Незмінна — виправлення лише сторно. */
@Serializable
data class JournalEntry(
    val id: String,
    val entityId: String,
    val entryDate: String,     // ISO-8601 (yyyy-MM-dd)
    val currency: String = "UAH",
    val lines: List<JournalLine> = emptyList(),
    val source: EntrySource = EntrySource.MANUAL,
    val reversed: Boolean = false,
    val memo: String? = null,
)

/** Джерело проводки (для provenance / immutability-cue в UI). */
enum class EntrySource { MANUAL, BANK, AR, AP, PAYROLL, CLOSING, REVERSING }

/** Обліковий період entity. */
@Serializable
data class AccountingPeriod(
    val id: String,
    val entityId: String,
    val label: String,         // напр. "2026-06"
    val state: PeriodState = PeriodState.OPEN,
)

/** Юрособа (multi-entity): своя функц. валюта + юрисдикція. */
@Serializable
data class Entity(
    val id: String,
    val name: String,
    val jurisdiction: Jurisdiction = Jurisdiction.UA,
    val functionalCurrency: String = "UAH",
)
