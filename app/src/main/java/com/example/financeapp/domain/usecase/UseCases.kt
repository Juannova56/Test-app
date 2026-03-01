package com.example.financeapp.domain.usecase

import com.example.financeapp.data.db.entity.AccountEntity
import com.example.financeapp.data.db.entity.TransactionEntity
import com.example.financeapp.domain.model.AccountType
import com.example.financeapp.domain.model.NetWorthSnapshot
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.AccountRepository
import com.example.financeapp.domain.repository.TransactionRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.YearMonth
import javax.inject.Inject

class TransferFundsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(fromAccountId: Long, toAccountId: Long, amountMinor: Long, epoch: Long) {
        require(amountMinor > 0) { "Transfer amount must be positive" }
        require(fromAccountId != toAccountId) { "Transfer accounts must be different" }

        val fromAccount = accountRepository.byId(fromAccountId) ?: error("from account not found")
        val toAccount = accountRepository.byId(toAccountId) ?: error("to account not found")

        if (fromAccount.strictMode && fromAccount.currentBalanceMinor < amountMinor) {
            error("Insufficient balance in strict mode")
        }

        accountRepository.update(fromAccount.copy(currentBalanceMinor = fromAccount.currentBalanceMinor - amountMinor))
        accountRepository.update(toAccount.copy(currentBalanceMinor = toAccount.currentBalanceMinor + amountMinor))

        transactionRepository.addTransfer(
            from = TransactionEntity(accountId = fromAccountId, type = TransactionType.TRANSFER_OUT, amountMinor = amountMinor, occurredAtEpoch = epoch),
            to = TransactionEntity(accountId = toAccountId, type = TransactionType.TRANSFER_IN, amountMinor = amountMinor, occurredAtEpoch = epoch),
        )
    }
}

class RecordCreditCardActivityUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend fun recordPurchase(cardAccountId: Long, amountMinor: Long, epoch: Long) {
        val card = requireAccountType(cardAccountId, AccountType.CREDIT_CARD)
        accountRepository.update(card.copy(currentBalanceMinor = card.currentBalanceMinor + amountMinor))
        transactionRepository.add(
            TransactionEntity(accountId = cardAccountId, type = TransactionType.CARD_PURCHASE, amountMinor = amountMinor, occurredAtEpoch = epoch)
        )
    }

    suspend fun recordPayment(cardAccountId: Long, cashAccountId: Long, amountMinor: Long, epoch: Long) {
        val card = requireAccountType(cardAccountId, AccountType.CREDIT_CARD)
        val cash = accountRepository.byId(cashAccountId) ?: error("cash account missing")
        if (cash.strictMode && cash.currentBalanceMinor < amountMinor) error("Insufficient cash balance")

        accountRepository.update(card.copy(currentBalanceMinor = (card.currentBalanceMinor - amountMinor).coerceAtLeast(0)))
        accountRepository.update(cash.copy(currentBalanceMinor = cash.currentBalanceMinor - amountMinor))

        transactionRepository.addTransfer(
            from = TransactionEntity(accountId = cashAccountId, type = TransactionType.CARD_PAYMENT, amountMinor = amountMinor, occurredAtEpoch = epoch),
            to = TransactionEntity(accountId = cardAccountId, type = TransactionType.CARD_PAYMENT, amountMinor = amountMinor, occurredAtEpoch = epoch),
        )
    }

    private suspend fun requireAccountType(accountId: Long, expected: AccountType): AccountEntity {
        val account = accountRepository.byId(accountId) ?: error("account missing")
        require(account.type == expected) { "Expected $expected account" }
        return account
    }
}

class ComputeNetWorthUseCase @Inject constructor() {
    operator fun invoke(accounts: List<AccountEntity>, now: LocalDateTime = LocalDateTime.now()): NetWorthSnapshot {
        val assets = accounts.filter { it.type != AccountType.CREDIT_CARD && it.type != AccountType.LOAN }
            .sumOf { it.currentBalanceMinor }
        val liabilities = accounts.filter { it.type == AccountType.CREDIT_CARD || it.type == AccountType.LOAN }
            .sumOf { it.currentBalanceMinor }
        return NetWorthSnapshot(capturedAt = now, assetsMinor = assets, liabilitiesMinor = liabilities)
    }
}

class MonthKeyResolverUseCase @Inject constructor() {
    operator fun invoke(epochMillis: Long, zoneId: ZoneId): String {
        val month = YearMonth.from(Instant.ofEpochMilli(epochMillis).atZone(zoneId))
        return month.toString()
    }
}
