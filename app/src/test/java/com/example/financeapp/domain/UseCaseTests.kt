package com.example.financeapp.domain

import com.example.financeapp.data.db.entity.AccountEntity
import com.example.financeapp.data.db.entity.BudgetEntity
import com.example.financeapp.data.db.entity.CreditCardEntity
import com.example.financeapp.data.db.entity.TransactionEntity
import com.example.financeapp.domain.model.AccountType
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.AccountRepository
import com.example.financeapp.domain.repository.BudgetRepository
import com.example.financeapp.domain.repository.CreditRepository
import com.example.financeapp.domain.repository.TransactionRepository
import com.example.financeapp.domain.usecase.ComputeNetWorthUseCase
import com.example.financeapp.domain.usecase.MonthKeyResolverUseCase
import com.example.financeapp.domain.usecase.RecordCreditCardActivityUseCase
import com.example.financeapp.domain.usecase.TransferFundsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId

class UseCaseTests {

    @Test
    fun transfer_moves_equal_amount_and_balances() = runBlocking {
        val repo = InMemoryAccountRepo(
            mutableListOf(
                AccountEntity(id = 1, name = "A", type = AccountType.CHECKING, currency = "USD", openingBalanceMinor = 1000, currentBalanceMinor = 1000, createdAtEpoch = 1),
                AccountEntity(id = 2, name = "B", type = AccountType.SAVINGS, currency = "USD", openingBalanceMinor = 100, currentBalanceMinor = 100, createdAtEpoch = 1),
            )
        )
        val tx = InMemoryTxRepo()
        TransferFundsUseCase(repo, tx).invoke(1, 2, 300, 11)

        assertEquals(700, repo.accounts.first { it.id == 1L }.currentBalanceMinor)
        assertEquals(400, repo.accounts.first { it.id == 2L }.currentBalanceMinor)
        assertEquals(TransactionType.TRANSFER_OUT, tx.items[0].type)
        assertEquals(TransactionType.TRANSFER_IN, tx.items[1].type)
        assertEquals(tx.items[0].amountMinor, tx.items[1].amountMinor)
    }

    @Test
    fun credit_purchase_and_payment_update_card_and_cash() = runBlocking {
        val repo = InMemoryAccountRepo(
            mutableListOf(
                AccountEntity(id = 1, name = "Card", type = AccountType.CREDIT_CARD, currency = "USD", openingBalanceMinor = 0, currentBalanceMinor = 0, createdAtEpoch = 1),
                AccountEntity(id = 2, name = "Cash", type = AccountType.CHECKING, currency = "USD", openingBalanceMinor = 1000, currentBalanceMinor = 1000, createdAtEpoch = 1),
            )
        )
        val tx = InMemoryTxRepo()
        val useCase = RecordCreditCardActivityUseCase(repo, tx)

        useCase.recordPurchase(1, 250, 1)
        useCase.recordPayment(1, 2, 100, 2)

        assertEquals(150, repo.accounts.first { it.id == 1L }.currentBalanceMinor)
        assertEquals(900, repo.accounts.first { it.id == 2L }.currentBalanceMinor)
    }

    @Test
    fun net_worth_subtracts_liabilities() {
        val useCase = ComputeNetWorthUseCase()
        val result = useCase(
            listOf(
                AccountEntity(id = 1, name = "Checking", type = AccountType.CHECKING, currency = "USD", openingBalanceMinor = 0, currentBalanceMinor = 1000, createdAtEpoch = 1),
                AccountEntity(id = 2, name = "Card", type = AccountType.CREDIT_CARD, currency = "USD", openingBalanceMinor = 0, currentBalanceMinor = 300, createdAtEpoch = 1),
                AccountEntity(id = 3, name = "Loan", type = AccountType.LOAN, currency = "USD", openingBalanceMinor = 0, currentBalanceMinor = 200, createdAtEpoch = 1),
            )
        )
        assertEquals(500, result.netWorthMinor)
    }

    @Test
    fun month_key_is_timezone_safe() {
        val useCase = MonthKeyResolverUseCase()
        val month = useCase(1719791999000, ZoneId.of("Asia/Tokyo"))
        assertEquals("2024-07", month)
    }
}

private class InMemoryAccountRepo(initial: MutableList<AccountEntity>) : AccountRepository {
    val accounts = initial
    private val state = MutableStateFlow(initial.toList())

    override fun observeAccounts(): Flow<List<AccountEntity>> = state.asStateFlow()

    override suspend fun create(account: AccountEntity): Long {
        accounts += account
        state.value = accounts.toList()
        return account.id
    }

    override suspend fun update(account: AccountEntity) {
        val idx = accounts.indexOfFirst { it.id == account.id }
        accounts[idx] = account
        state.value = accounts.toList()
    }

    override suspend fun byId(id: Long): AccountEntity? = accounts.firstOrNull { it.id == id }
}

private class InMemoryTxRepo : TransactionRepository {
    val items = mutableListOf<TransactionEntity>()
    private val state = MutableStateFlow(emptyList<TransactionEntity>())
    override fun observeTransactions(): Flow<List<TransactionEntity>> = state.asStateFlow()
    override suspend fun add(transaction: TransactionEntity): Long {
        items += transaction
        state.value = items.toList()
        return items.size.toLong()
    }

    override suspend fun addTransfer(from: TransactionEntity, to: TransactionEntity) {
        items += listOf(from, to)
        state.value = items.toList()
    }
}
