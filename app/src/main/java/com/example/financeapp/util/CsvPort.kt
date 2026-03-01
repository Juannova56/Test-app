package com.example.financeapp.util

import com.example.financeapp.data.db.entity.AccountEntity
import com.example.financeapp.data.db.entity.TransactionEntity
import com.example.financeapp.domain.model.AccountType
import com.example.financeapp.domain.model.TransactionType

object CsvPort {
    const val ACCOUNT_HEADER = "id,name,type,currency,opening_balance_minor,current_balance_minor,strict_mode,archived,created_at_epoch"
    const val TX_HEADER = "id,account_id,type,amount_minor,occurred_at_epoch,category_id,merchant,notes,tags_csv,linked_transfer_id"

    fun exportAccounts(accounts: List<AccountEntity>): String = buildString {
        appendLine(ACCOUNT_HEADER)
        accounts.forEach {
            appendLine(listOf(it.id, it.name, it.type, it.currency, it.openingBalanceMinor, it.currentBalanceMinor, it.strictMode, it.archived, it.createdAtEpoch).joinToString(","))
        }
    }

    fun exportTransactions(transactions: List<TransactionEntity>): String = buildString {
        appendLine(TX_HEADER)
        transactions.forEach {
            appendLine(listOf(it.id, it.accountId, it.type, it.amountMinor, it.occurredAtEpoch, it.categoryId ?: "", it.merchant ?: "", it.notes ?: "", it.tagsCsv ?: "", it.linkedTransferId ?: "").joinToString(","))
        }
    }

    fun parseAccounts(csv: String): List<AccountEntity> = csv.lineSequence().drop(1).filter { it.isNotBlank() }.map { line ->
        val p = line.split(',')
        AccountEntity(
            id = p[0].toLong(),
            name = p[1],
            type = AccountType.valueOf(p[2]),
            currency = p[3],
            openingBalanceMinor = p[4].toLong(),
            currentBalanceMinor = p[5].toLong(),
            strictMode = p[6].toBoolean(),
            archived = p[7].toBoolean(),
            createdAtEpoch = p[8].toLong(),
        )
    }.toList()

    fun parseTransactions(csv: String): List<TransactionEntity> = csv.lineSequence().drop(1).filter { it.isNotBlank() }.map { line ->
        val p = line.split(',')
        TransactionEntity(
            id = p[0].toLong(),
            accountId = p[1].toLong(),
            type = TransactionType.valueOf(p[2]),
            amountMinor = p[3].toLong(),
            occurredAtEpoch = p[4].toLong(),
            categoryId = p[5].takeIf { it.isNotBlank() }?.toLong(),
            merchant = p[6].ifBlank { null },
            notes = p[7].ifBlank { null },
            tagsCsv = p[8].ifBlank { null },
            linkedTransferId = p.getOrNull(9)?.takeIf { it.isNotBlank() }?.toLong(),
        )
    }.toList()
}
