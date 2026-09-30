package com.rising.pos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.rising.pos.core.database.entity.TransactionEntity
import com.rising.pos.core.database.entity.TransactionItemEntity
import com.rising.pos.core.database.entity.TransactionItemModifierEntity
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions WHERE status != 'HELD' ORDER BY created_at DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 100): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionWithDetails?

    @Transaction
    @Query("SELECT * FROM transactions WHERE receipt_number = :receiptNumber LIMIT 1")
    suspend fun getTransactionByReceiptNumber(receiptNumber: String): TransactionWithDetails?

    @Transaction
    @Query("SELECT * FROM transactions WHERE created_at >= :startDate AND created_at <= :endDate AND status = 'COMPLETED' ORDER BY created_at DESC")
    fun getCompletedTransactionsBetween(startDate: Long, endDate: Long): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE status = 'HELD' ORDER BY created_at DESC")
    fun getHeldTransactions(): Flow<List<TransactionWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItemModifiers(modifiers: List<TransactionItemModifierEntity>)

    @Query("UPDATE transactions SET status = :status WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus)

    @Query("SELECT SUM(grand_total) FROM transactions WHERE status = 'COMPLETED' AND created_at >= :startDate AND created_at <= :endDate")
    fun getGrossSalesBetween(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions WHERE status = 'COMPLETED' AND created_at >= :startDate AND created_at <= :endDate")
    fun getTransactionCountBetween(startDate: Long, endDate: Long): Flow<Int>

    @Transaction
    @Query("SELECT * FROM transactions WHERE customer_id = :customerId ORDER BY created_at DESC")
    fun getTransactionsByCustomerId(customerId: String): Flow<List<TransactionWithDetails>>

    @Query("SELECT COUNT(*) FROM transactions WHERE receipt_number LIKE :prefix || '%'")
    suspend fun countTransactionsWithPrefix(prefix: String): Int

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Query("DELETE FROM transaction_items WHERE transaction_id = :transactionId")
    suspend fun deleteTransactionItems(transactionId: String)

    @Query("DELETE FROM transaction_item_modifiers WHERE transaction_item_id IN (SELECT id FROM transaction_items WHERE transaction_id = :transactionId)")
    suspend fun deleteTransactionItemModifiers(transactionId: String)
}

