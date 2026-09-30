package com.rising.pos.data.repository

import androidx.room.withTransaction
import com.rising.pos.core.database.PosDatabase
import com.rising.pos.core.database.dao.ProductDao
import com.rising.pos.core.database.dao.StockMovementDao
import com.rising.pos.core.database.dao.TransactionDao
import com.rising.pos.core.database.entity.StockMovementEntity
import com.rising.pos.core.database.entity.TransactionEntity
import com.rising.pos.core.database.entity.TransactionItemEntity
import com.rising.pos.core.database.entity.TransactionItemModifierEntity
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.core.model.StockMovementType
import com.rising.pos.core.model.SyncStatus
import com.rising.pos.core.model.TransactionStatus
import com.rising.pos.core.util.ReceiptNumberGenerator
import com.rising.pos.domain.model.CartState
import com.rising.pos.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val database: PosDatabase,
    private val transactionDao: TransactionDao,
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao
) : TransactionRepository {

    private val dateOnlyFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    override suspend fun processCheckout(
        cartState: CartState,
        paymentMethod: PaymentMethod,
        paymentAmount: Double,
        deviceId: String,
        cashierId: String?,
        isTaxEnabled: Boolean,
        taxPercentage: Double,
        isTaxInclusive: Boolean,
        isServiceChargeEnabled: Boolean,
        serviceChargePercentage: Double
    ): Result<TransactionWithDetails> = runCatching {
        if (cartState.items.isEmpty()) {
            throw IllegalArgumentException("Keranjang belanja kosong")
        }

        database.withTransaction {
            val now = System.currentTimeMillis()
            val transactionId = UUID.randomUUID().toString()
            val dateStr = dateOnlyFormat.format(Date(now))
            val prefix = "TRX-$deviceId-$dateStr"
            val countToday = transactionDao.countTransactionsWithPrefix(prefix)
            val receiptNumber = ReceiptNumberGenerator.generate(deviceId, countToday + 1, now)

            val calc = cartState.calculateTotals(
                isTaxEnabled = isTaxEnabled,
                taxPercentage = taxPercentage,
                isTaxInclusive = isTaxInclusive,
                isServiceChargeEnabled = isServiceChargeEnabled,
                serviceChargePercentage = serviceChargePercentage
            )

            if (paymentMethod == PaymentMethod.CASH && paymentAmount < calc.grandTotal) {
                throw IllegalArgumentException("Jumlah pembayaran tunai kurang dari total belanja")
            }

            val changeAmount = if (paymentMethod == PaymentMethod.CASH) {
                maxOf(0.0, paymentAmount - calc.grandTotal)
            } else {
                0.0
            }

            // 1. Insert Transaction record
            val transactionEntity = TransactionEntity(
                id = transactionId,
                receiptNumber = receiptNumber,
                deviceId = deviceId,
                cashierId = cashierId,
                customerId = cartState.customer?.id,
                orderType = cartState.orderType,
                subtotal = calc.subtotal,
                discount = calc.discount,
                discountReason = cartState.discountReason,
                tax = calc.tax,
                serviceCharge = calc.serviceCharge,
                grandTotal = calc.grandTotal,
                paymentAmount = paymentAmount,
                changeAmount = changeAmount,
                paymentMethod = paymentMethod,
                status = TransactionStatus.COMPLETED,
                note = cartState.note,
                createdAt = now,
                syncStatus = SyncStatus.PENDING
            )
            transactionDao.insertTransaction(transactionEntity)

            // 2. Insert Transaction Items & Modifiers
            val itemEntities = mutableListOf<TransactionItemEntity>()
            val modifierEntities = mutableListOf<TransactionItemModifierEntity>()
            val stockMovements = mutableListOf<StockMovementEntity>()

            for (cartItem in cartState.items) {
                val itemId = UUID.randomUUID().toString()
                itemEntities.add(
                    TransactionItemEntity(
                        id = itemId,
                        transactionId = transactionId,
                        productId = cartItem.product.id,
                        productName = cartItem.product.name,
                        variantName = cartItem.variant?.name,
                        qty = cartItem.quantity,
                        unitPrice = cartItem.unitPrice,
                        subtotal = cartItem.totalPrice,
                        discount = cartItem.discount,
                        note = cartItem.note
                    )
                )

                // Modifiers
                for (mod in cartItem.selectedModifiers) {
                    modifierEntities.add(
                        TransactionItemModifierEntity(
                            id = UUID.randomUUID().toString(),
                            transactionItemId = itemId,
                            modifierId = mod.id,
                            modifierName = mod.name,
                            price = mod.price
                        )
                    )
                }

                // 3. Stock Movement & Update Stock (Event-based Stock Ledger)
                if (cartItem.product.trackStock) {
                    val currentProduct = productDao.getProductById(cartItem.product.id)
                    val beforeStock = currentProduct?.stock ?: 0.0
                    val afterStock = beforeStock - cartItem.quantity

                    productDao.updateStock(cartItem.product.id, afterStock, now)

                    stockMovements.add(
                        StockMovementEntity(
                            id = UUID.randomUUID().toString(),
                            productId = cartItem.product.id,
                            type = StockMovementType.SALE,
                            qtyChange = -cartItem.quantity,
                            qtyBefore = beforeStock,
                            qtyAfter = afterStock,
                            reason = "Penjualan #$receiptNumber",
                            referenceId = transactionId,
                            createdAt = now,
                            syncStatus = SyncStatus.PENDING
                        )
                    )
                }
            }

            transactionDao.insertTransactionItems(itemEntities)
            if (modifierEntities.isNotEmpty()) {
                transactionDao.insertTransactionItemModifiers(modifierEntities)
            }
            if (stockMovements.isNotEmpty()) {
                stockMovementDao.insertMovements(stockMovements)
            }

            transactionDao.getTransactionById(transactionId)
                ?: throw IllegalStateException("Gagal memuat detail transaksi yang baru dibuat")
        }
    }

    override fun getRecentTransactions(limit: Int): Flow<List<TransactionWithDetails>> =
        transactionDao.getRecentTransactions(limit)

    override suspend fun getTransactionById(id: String): TransactionWithDetails? =
        transactionDao.getTransactionById(id)

    override suspend fun getTransactionByReceiptNumber(receiptNumber: String): TransactionWithDetails? =
        transactionDao.getTransactionByReceiptNumber(receiptNumber)

    override fun getCompletedTransactionsBetween(
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithDetails>> =
        transactionDao.getCompletedTransactionsBetween(startDate, endDate)

    override fun getHeldTransactions(): Flow<List<TransactionWithDetails>> =
        transactionDao.getHeldTransactions()

    override suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus) {
        transactionDao.updateTransactionStatus(transactionId, status)
    }

    override fun getGrossSalesBetween(startDate: Long, endDate: Long): Flow<Double?> =
        transactionDao.getGrossSalesBetween(startDate, endDate)

    override fun getTransactionCountBetween(startDate: Long, endDate: Long): Flow<Int> =
        transactionDao.getTransactionCountBetween(startDate, endDate)
}
