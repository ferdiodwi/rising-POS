package com.rising.pos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class CustomerWithStats(
    @Embedded
    val customer: CustomerEntity,
    @ColumnInfo(name = "total_transactions")
    val totalTransactions: Int = 0,
    @ColumnInfo(name = "total_spent")
    val totalSpent: Long = 0L,
    @ColumnInfo(name = "last_transaction_date")
    val lastTransactionDate: Long? = null
)
