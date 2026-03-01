package com.example.financeapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.financeapp.data.db.dao.AccountDao
import com.example.financeapp.data.db.dao.BudgetDao
import com.example.financeapp.data.db.dao.CreditCardDao
import com.example.financeapp.data.db.dao.LoanDao
import com.example.financeapp.data.db.dao.TransactionDao
import com.example.financeapp.data.db.entity.AccountEntity
import com.example.financeapp.data.db.entity.AttachmentEntity
import com.example.financeapp.data.db.entity.BudgetEntity
import com.example.financeapp.data.db.entity.CategoryEntity
import com.example.financeapp.data.db.entity.CreditCardEntity
import com.example.financeapp.data.db.entity.LoanEntity
import com.example.financeapp.data.db.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        CreditCardEntity::class,
        LoanEntity::class,
        AttachmentEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun loanDao(): LoanDao
}
