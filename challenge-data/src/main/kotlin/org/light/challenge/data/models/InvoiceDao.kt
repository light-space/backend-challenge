package org.light.challenge.data.models

import org.javamoney.moneta.Money

data class InvoiceDao(
    val id: InvoiceId,
    val companyId: CompanyId,
    val status: InvoiceStatus,
    val amount: Money,
    val department: Department,
    val requitesApproval: Boolean
)

enum class InvoiceStatus {
    PENDING,
    APPROVED
}

enum class Department {
    FINANCE,
    MARKETING
}