package com.example.financeapp.data.repository

import com.example.financeapp.data.db.dao.AccountDao
import com.example.financeapp.data.db.dao.BudgetDao
import com.example.financeapp.data.db.dao.CreditCardDao
import com.example.financeapp.data.db.dao.TransactionDao
import com.example.financeapp.data.db.entity.AccountEntity
import com.example.financeapp.data.db.entity.BudgetEntity
import com.example.financeapp.data.db.entity.CreditCardEntity
import com.example.financeapp.data.db.entity.TransactionEntity
import com.example.financeapp.domain.repository.AccountRepository
import com.example.financeapp.domain.repository.BudgetRepository
import com.example.financeapp.domain.repository.CreditRepository
import com.example.financeapp.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
) : AccountRepository {
    override fun observeAccounts(): Flow<List<AccountEntity>> = accountDao.observeAccounts()
    override suspend fun create(account: AccountEntity): Long = accountDao.insert(account)
    override suspend fun update(account: AccountEntity) = accountDao.update(account)
    override suspend fun byId(id: Long): AccountEntity? = accountDao.getById(id)
}

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
) : TransactionRepository {
    override fun observeTransactions(): Flow<List<TransactionEntity>> = transactionDao.observeAll()
    override suspend fun add(transaction: TransactionEntity): Long = transactionDao.insert(transaction)

    override suspend fun addTransfer(from: TransactionEntity, to: TransactionEntity) {
        transactionDao.insertAll(listOf(from, to))
    }
}

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
) : BudgetRepository {
    override fun observeMonthlyBudgets(monthKey: String): Flow<List<BudgetEntity>> = budgetDao.observeByMonth(monthKey)
    override suspend fun upsert(budget: BudgetEntity) { budgetDao.insert(budget) }
}

class CreditRepositoryImpl @Inject constructor(
    private val creditCardDao: CreditCardDao,
) : CreditRepository {
    override fun observeCards(): Flow<List<CreditCardEntity>> = creditCardDao.observeAll()
    override suspend fun createCard(card: CreditCardEntity): Long = creditCardDao.insert(card)
}
