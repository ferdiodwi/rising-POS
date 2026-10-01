package com.rising.pos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.rising.pos.core.database.entity.TopSellingProduct
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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItemModifiers(modifiers: List<TransactionItemModifierEntity>)

    @Query("UPDATE transactions SET status = :status WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus)

    @Query("SELECT SUM(grand_total) FROM transactions WHERE status = 'COMPLETED' AND created_at >= :startDate AND created_at <= :endDate")
    fun getGrossSalesBetween(startDate: Long, endDate: Long): Flow<Long?>

    @Query("SELECT COUNT(*) FROM transactions WHERE status = 'COMPLETED' AND created_at >= :startDate AND created_at <= :endDate")
    fun getTransactionCountBetween(startDate: Long, endDate: Long): Flow<Int>

    @Query("""
        SELECT ti.product_id, ti.product_name, SUM(ti.qty) AS total_qty, SUM(ti.subtotal) AS total_revenue
        FROM transaction_items ti
        INNER JOIN transactions t ON ti.transaction_id = t.id
        WHERE t.status = 'COMPLETED' AND t.created_at >= :startDate AND t.created_at <= :endDate
        GROUP BY ti.product_id, ti.product_name
        ORDER BY total_qty DESC
        LIMIT :limit
    """)
    fun getTopSellingProductsBetween(startDate: Long, endDate: Long, limit: Int = 5): Flow<List<TopSellingProduct>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE customer_id = :customerId ORDER BY created_at DESC")
    fun getTransactionsByCustomerId(customerId: String): Flow<List<TransactionWithDetails>>

    /**
     * Mengembalikan nomor urut terbesar yang sudah terpakai untuk [prefix]
     * (mis. "TRX-A01-20260930-"), atau 0 bila belum ada.
     *
     * Memakai MAX(...) bukan COUNT(*) supaya nomor tidak pernah menabrak nomor
     * yang sudah terpakai ketika ada transaksi yang dihapus. Pemanggil WAJIB
     * berada di dalam `database.withTransaction { }` agar operasi baca-lalu-tulis
     * (baca MAX di sini, lalu INSERT nomor +1) tetap atomik.
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTR(receipt_number, LENGTH(:prefix) + 1) AS INTEGER)), 0) FROM transactions WHERE receipt_number LIKE :prefix || '%'")
    suspend fun nextReceiptSequence(prefix: String): Int

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Query("DELETE FROM transaction_items WHERE transaction_id = :transactionId")
    suspend fun deleteTransactionItems(transactionId: String)

    @Query("DELETE FROM transaction_item_modifiers WHERE transaction_item_id IN (SELECT id FROM transaction_items WHERE transaction_id = :transactionId)")
    suspend fun deleteTransactionItemModifiers(transactionId: String)
}

