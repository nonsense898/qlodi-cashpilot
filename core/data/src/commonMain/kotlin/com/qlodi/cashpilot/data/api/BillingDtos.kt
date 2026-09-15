package com.qlodi.cashpilot.data.api

import kotlinx.serialization.Serializable

/*
 * Billing shared with Qlodi Business: the same /v1/clients and /v1/invoices records.
 * Statuses and sources stay plain strings so a new backend value never breaks decoding.
 * Fields the backend requires have no Kotlin default, so they are always serialized.
 */

@Serializable
data class ClientDto(
    val id: String,
    val workspaceId: String = "",
    val name: String,
    val legalName: String? = null,
    val billingEmail: String,
    val currency: String,
    val paymentTermsDays: Int = 30,
    val taxId: String? = null,
    val status: String = "Active",
)

@Serializable
data class InvoiceLineDto(
    val id: String,
    val invoiceId: String,
    val description: String,
    val qty: Double = 1.0,
    val unitPrice: Double,
    val amount: Double = 0.0,
    val taxRate: Double = 0.0,
)

@Serializable
data class InvoiceDto(
    val id: String,
    val number: String = "",
    val clientId: String,
    val issueDate: String,
    val dueDate: String,
    val currency: String,
    val source: String,
    val status: String = "Draft",
    val subtotal: Double = 0.0,
    val taxTotal: Double = 0.0,
    val total: Double = 0.0,
    val amountPaid: Double = 0.0,
    val notes: String? = null,
    val sentAt: String? = null,
    val paidAt: String? = null,
    val publicToken: String? = null,
    /** CashPilot company whose ledger gets the entries; null = first company (invoices made in Business). */
    val entityId: String? = null,
    val lines: List<InvoiceLineDto> = emptyList(),
)

@Serializable
data class InvoicePaymentRequest(val amount: Double, val paidAt: String? = null)
