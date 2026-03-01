package com.example.financeapp.data.db

import androidx.room.TypeConverter
import com.example.financeapp.domain.model.AccountType
import com.example.financeapp.domain.model.TransactionType

class Converters {
    @TypeConverter fun accountTypeToString(value: AccountType): String = value.name
    @TypeConverter fun stringToAccountType(value: String): AccountType = AccountType.valueOf(value)
    @TypeConverter fun txTypeToString(value: TransactionType): String = value.name
    @TypeConverter fun stringToTxType(value: String): TransactionType = TransactionType.valueOf(value)
}
