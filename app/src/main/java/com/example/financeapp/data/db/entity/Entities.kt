package com.example.financeapp.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.financeapp.domain.model.AccountType
import com.example.financeapp.domain.model.TransactionType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val currency: String,
    val openingBalanceMinor: Long,
    val currentBalanceMinor: Long,
    val strictMode: Boolean = false,
    val archived: Boolean = false,
    val createdAtEpoch: Long,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentCategoryId: Long? = null,
    val kind: String,
)

@Entity(tableName = "transactions", indices = [Index("accountId"), Index("linkedTransferId")])
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val type: TransactionType,
    val amountMinor: Long,
    val occurredAtEpoch: Long,
    val categoryId: Long? = null,
    val merchant: String? = null,
    val notes: String? = null,
    val tagsCsv: String? = null,
    val linkedTransferId: Long? = null,
)

@Entity(
    tableName = "budgets",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("categoryId")]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val monthKey: String,
    val amountMinor: Long,
)

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val creditLimitMinor: Long,
    val aprBps: Int,
    val statementStartDay: Int,
    val statementEndDay: Int,
    val dueDayOfMonth: Int,
    val minimumPaymentRateBps: Int = 200,
)

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val principalMinor: Long,
    val annualRateBps: Int,
    val remainingPrincipalMinor: Long,
    val startedAtEpoch: Long,
)

@Entity(tableName = "attachments")
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val uri: String,
    val mimeType: String,
    val byteSize: Long,
)
