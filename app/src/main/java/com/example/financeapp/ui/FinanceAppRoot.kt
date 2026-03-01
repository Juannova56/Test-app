package com.example.financeapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financeapp.data.db.entity.AccountEntity
import com.example.financeapp.data.db.entity.TransactionEntity
import com.example.financeapp.ui.navigation.RootTab
import com.example.financeapp.ui.viewmodel.MainViewModel
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAppRoot(vm: MainViewModel = hiltViewModel()) {
    var selectedTab by rememberSaveable { mutableStateOf(RootTab.Dashboard) }
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val transactions by vm.transactions.collectAsStateWithLifecycle()
    val dashboard by vm.dashboard.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.seedIfEmpty() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val first = accounts.firstOrNull() ?: return@FloatingActionButton
                vm.addExpense(first.id, 12_50, "Coffee")
            }) {
                Text("+")
            }
        },
        bottomBar = {
            BottomAppBar {
                RootTab.entries.forEach { tab ->
                    Button(onClick = { selectedTab = tab }, modifier = Modifier.padding(horizontal = 4.dp)) {
                        Text(tab.label)
                    }
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            RootTab.Dashboard -> DashboardScreen(dashboard.netWorthMinor, dashboard.monthIncomeMinor, dashboard.monthExpenseMinor, Modifier.padding(padding))
            RootTab.Transactions -> TransactionsScreen(transactions, Modifier.padding(padding))
            RootTab.Accounts -> AccountsScreen(accounts, Modifier.padding(padding))
            RootTab.Credit -> CreditScreen(accounts, Modifier.padding(padding))
            RootTab.Settings -> SettingsScreen(Modifier.padding(padding))
        }
    }
}

@Composable
fun DashboardScreen(netWorth: Long, income: Long, expenses: Long, modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Net Worth: ${formatMinor(netWorth)}", style = MaterialTheme.typography.headlineSmall)
        Text("Income: ${formatMinor(income)}")
        Text("Expenses: ${formatMinor(expenses)}")
        Text("Charts placeholder: monthly bar + category pie")
    }
}

@Composable
fun TransactionsScreen(transactions: List<TransactionEntity>, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Transactions (filter/search ready for integration)") }
        items(transactions) { tx ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(tx.type.name)
                    Text(formatMinor(tx.amountMinor))
                }
            }
        }
    }
}

@Composable
fun AccountsScreen(accounts: List<AccountEntity>, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Accounts") }
        items(accounts) { account ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(account.name)
                    Text(formatMinor(account.currentBalanceMinor))
                }
            }
        }
    }
}

@Composable
fun CreditScreen(accounts: List<AccountEntity>, modifier: Modifier = Modifier) {
    val cards = accounts.filter { it.type.name == "CREDIT_CARD" }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Credit Cards")
        cards.forEach {
            Text("${it.name}: ${formatMinor(it.currentBalanceMinor)}")
        }
        Text("Loan support in data model + use cases; detail screen can be expanded.")
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Settings")
        Text("- Currency/Locale")
        Text("- Strict Mode (prevent negative balances)")
        Text("- CSV import/export")
        Text("- App lock placeholder (biometric/PIN)")
    }
}

private fun formatMinor(minor: Long): String = NumberFormat.getCurrencyInstance().format(minor / 100.0)
