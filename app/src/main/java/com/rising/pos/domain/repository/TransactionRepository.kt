package com.rising.pos.domain.repository

import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.core.model.TransactionStatus
import com.rising.pos.domain.model.CartState
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun processCheckout(
        cartState: CartState,
        paymentMethod: PaymentMethod,
        paymentAmount: Double,
        deviceId: String,
        cashierId: String? = null,
        isTaxEnabled: Boolean = false,
        taxPercentage: Double = 0.0,
        isTaxInclusive: Boolean = true,
        isServiceChargeEnabled: Boolean = false,
        serviceChargePercentage: Double = 0.0
    ): Result<TransactionWithDetails>

    fun getRecentTransactions(limit: Int = 100): Flow<List<TransactionWithDetails>>
    suspend fun getTransactionById(id: String): TransactionWithDetails?
    suspend fun getTransactionByReceiptNumber(receiptNumber: String): TransactionWithDetails?
    fun getCompletedTransactionsBetween(startDate: Long, endDate: Long): Flow<List<TransactionWithDetails>>
    fun getHeldTransactions(): Flow<List<TransactionWithDetails>>
    suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus)
    fun getGrossSalesBetween(startDate: Long, endDate: Long): Flow<Double?>
    fun getTransactionCountBetween(startDate: Long, endDate: Long): Flow<Int>
}
