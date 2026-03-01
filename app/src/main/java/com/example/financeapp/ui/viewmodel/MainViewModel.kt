package com.example.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.db.entity.AccountEntity
import com.example.financeapp.data.db.entity.TransactionEntity
import com.example.financeapp.domain.model.AccountType
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.AccountRepository
import com.example.financeapp.domain.repository.TransactionRepository
import com.example.financeapp.domain.usecase.ComputeNetWorthUseCase
import com.example.financeapp.domain.usecase.TransferFundsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val transferFundsUseCase: TransferFundsUseCase,
    private val computeNetWorthUseCase: ComputeNetWorthUseCase,
) : ViewModel() {

    val accounts = accountRepository.observeAccounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactions = transactionRepository.observeTransactions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboard = combine(accounts, transactions) { a, t ->
        val netWorth = computeNetWorthUseCase(a)
        DashboardUiState(
            totalAssetsMinor = netWorth.assetsMinor,
            totalLiabilitiesMinor = netWorth.liabilitiesMinor,
            netWorthMinor = netWorth.netWorthMinor,
            monthIncomeMinor = t.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor },
            monthExpenseMinor = t.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun seedIfEmpty() = viewModelScope.launch {
        if (accounts.value.isEmpty()) {
            accountRepository.create(
                AccountEntity(
                    name = "Checking",
                    type = AccountType.CHECKING,
                    currency = "USD",
                    openingBalanceMinor = 500_00,
                    currentBalanceMinor = 500_00,
                    createdAtEpoch = Instant.now().toEpochMilli(),
                )
            )
        }
    }

    fun addExpense(accountId: Long, amountMinor: Long, merchant: String) = viewModelScope.launch {
        val account = accountRepository.byId(accountId) ?: return@launch
        if (account.strictMode && account.currentBalanceMinor < amountMinor) return@launch
        accountRepository.update(account.copy(currentBalanceMinor = account.currentBalanceMinor - amountMinor))
        transactionRepository.add(
            TransactionEntity(
                accountId = accountId,
                type = TransactionType.EXPENSE,
                amountMinor = amountMinor,
                occurredAtEpoch = Instant.now().toEpochMilli(),
                merchant = merchant,
            )
        )
    }

    fun quickTransfer(from: Long, to: Long, amountMinor: Long) = viewModelScope.launch {
        transferFundsUseCase(from, to, amountMinor, Instant.now().toEpochMilli())
    }
}

data class DashboardUiState(
    val totalAssetsMinor: Long = 0,
    val totalLiabilitiesMinor: Long = 0,
    val netWorthMinor: Long = 0,
    val monthIncomeMinor: Long = 0,
    val monthExpenseMinor: Long = 0,
)
