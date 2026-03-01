package com.example.financeapp.domain.model

import java.time.LocalDateTime

enum class AccountType { CASH, CHECKING, SAVINGS, EWALLET, CREDIT_CARD, LOAN }
enum class TransactionType { EXPENSE, INCOME, TRANSFER_OUT, TRANSFER_IN, REFUND, ADJUSTMENT, CARD_PURCHASE, CARD_PAYMENT, LOAN_PAYMENT }
enum class EntryDirection { DEBIT, CREDIT }

data class Money(val minor: Long, val currency: String = "USD")

data class MonthlySummary(
    val month: String,
    val incomeMinor: Long,
    val expenseMinor: Long
)

data class NetWorthSnapshot(
    val capturedAt: LocalDateTime,
    val assetsMinor: Long,
    val liabilitiesMinor: Long
) {
    val netWorthMinor: Long get() = assetsMinor - liabilitiesMinor
}
