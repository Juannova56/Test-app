package com.example.financeapp.domain.repository

import com.example.financeapp.data.db.entity.AccountEntity
import com.example.financeapp.data.db.entity.BudgetEntity
import com.example.financeapp.data.db.entity.CreditCardEntity
import com.example.financeapp.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<AccountEntity>>
    suspend fun create(account: AccountEntity): Long
    suspend fun update(account: AccountEntity)
    suspend fun byId(id: Long): AccountEntity?
}

interface TransactionRepository {
    fun observeTransactions(): Flow<List<TransactionEntity>>
    suspend fun add(transaction: TransactionEntity): Long
    suspend fun addTransfer(from: TransactionEntity, to: TransactionEntity)
}

interface BudgetRepository {
    fun observeMonthlyBudgets(monthKey: String): Flow<List<BudgetEntity>>
    suspend fun upsert(budget: BudgetEntity)
}

interface CreditRepository {
    fun observeCards(): Flow<List<CreditCardEntity>>
    suspend fun createCard(card: CreditCardEntity): Long
}
