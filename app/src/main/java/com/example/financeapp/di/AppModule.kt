package com.example.financeapp.di

import android.content.Context
import androidx.room.Room
import com.example.financeapp.data.db.AppDatabase
import com.example.financeapp.data.repository.AccountRepositoryImpl
import com.example.financeapp.data.repository.BudgetRepositoryImpl
import com.example.financeapp.data.repository.CreditRepositoryImpl
import com.example.financeapp.data.repository.TransactionRepositoryImpl
import com.example.financeapp.domain.repository.AccountRepository
import com.example.financeapp.domain.repository.BudgetRepository
import com.example.financeapp.domain.repository.CreditRepository
import com.example.financeapp.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "finance.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun accountDao(db: AppDatabase) = db.accountDao()
    @Provides fun transactionDao(db: AppDatabase) = db.transactionDao()
    @Provides fun budgetDao(db: AppDatabase) = db.budgetDao()
    @Provides fun creditCardDao(db: AppDatabase) = db.creditCardDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository
    @Binds abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository
    @Binds abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository
    @Binds abstract fun bindCreditRepository(impl: CreditRepositoryImpl): CreditRepository
}
