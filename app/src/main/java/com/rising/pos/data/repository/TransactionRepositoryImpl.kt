package com.rising.pos.data.repository

import androidx.room.withTransaction
import com.rising.pos.core.database.PosDatabase
import com.rising.pos.core.database.dao.ProductDao
import com.rising.pos.core.database.dao.StockMovementDao
import com.rising.pos.core.database.dao.TransactionDao
import com.rising.pos.core.database.entity.StockMovementEntity
import com.rising.pos.core.database.entity.TopSellingProduct
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
        paymentAmount: Long,
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
            // Nomor urut diambil dari MAX nomor yang sudah ada + 1, di dalam transaksi DB.
            // COUNT(*) berbahaya: bila ada transaksi yang dihapus, nomor hasilnya bisa
            // menabrak nomor yang sudah terpakai, dan operasinya tidak atomik.
            val nextSeq = transactionDao.nextReceiptSequence("TRX-$deviceId-$dateStr-") + 1
            val receiptNumber = ReceiptNumberGenerator.generate(deviceId, nextSeq, now)

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
                (paymentAmount - calc.grandTotal).coerceAtLeast(0L)
            } else {
                0L
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

                    // Validasi stok: jangan biarkan penjualan melebihi stok yang tersedia,
                    // karena stok negatif merusak laporan inventori & valuasi. Dilempar di
                    // dalam withTransaction agar seluruh checkout di-rollback bila gagal.
                    if (beforeStock < cartItem.quantity) {
                        throw IllegalStateException(
                            "Stok ${cartItem.product.name} tidak cukup " +
                                "(tersedia ${beforeStock.toInt()}, diminta ${cartItem.quantity.toInt()})."
                        )
                    }

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

    override fun getTransactionsBetween(startDate: Long, endDate: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.getTransactionsBetween(startDate, endDate)

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

    override suspend fun voidTransaction(transactionId: String, reason: String): Result<Unit> = runCatching {
        database.withTransaction {
            val trxDetails = transactionDao.getTransactionById(transactionId)
                ?: throw IllegalArgumentException("Transaksi tidak ditemukan")
            val trx = trxDetails.transaction
            if (trx.status != TransactionStatus.COMPLETED) {
                throw IllegalStateException("Hanya transaksi selesai yang dapat dibatalkan")
            }

            val now = System.currentTimeMillis()
            transactionDao.updateTransactionStatus(transactionId, TransactionStatus.CANCELLED)

            // Kembalikan stok produk otomatis
            for (itemDetail in trxDetails.items) {
                val item = itemDetail.item
                val product = productDao.getProductById(item.productId)
                if (product != null && product.trackStock) {
                    val before = product.stock
                    val after = before + item.qty
                    productDao.updateStock(product.id, after, now)

                    stockMovementDao.insertMovement(
                        StockMovementEntity(
                            id = UUID.randomUUID().toString(),
                            productId = product.id,
                            type = StockMovementType.REFUND,
                            qtyChange = item.qty,
                            qtyBefore = before,
                            qtyAfter = after,
                            reason = "Void #${trx.receiptNumber}: $reason",
                            referenceId = transactionId,
                            createdAt = now,
                            syncStatus = SyncStatus.PENDING
                        )
                    )
                }
            }
        }
    }

    override suspend fun refundTransaction(transactionId: String, reason: String): Result<Unit> = runCatching {
        database.withTransaction {
            val trxDetails = transactionDao.getTransactionById(transactionId)
                ?: throw IllegalArgumentException("Transaksi tidak ditemukan")
            val trx = trxDetails.transaction
            if (trx.status != TransactionStatus.COMPLETED) {
                throw IllegalStateException("Hanya transaksi selesai yang dapat di-refund")
            }

            val now = System.currentTimeMillis()
            transactionDao.updateTransactionStatus(transactionId, TransactionStatus.REFUNDED)

            for (itemDetail in trxDetails.items) {
                val item = itemDetail.item
                val product = productDao.getProductById(item.productId)
                if (product != null && product.trackStock) {
                    val before = product.stock
                    val after = before + item.qty
                    productDao.updateStock(product.id, after, now)

                    stockMovementDao.insertMovement(
                        StockMovementEntity(
                            id = UUID.randomUUID().toString(),
                            productId = product.id,
                            type = StockMovementType.REFUND,
                            qtyChange = item.qty,
                            qtyBefore = before,
                            qtyAfter = after,
                            reason = "Refund #${trx.receiptNumber}: $reason",
                            referenceId = transactionId,
                            createdAt = now,
                            syncStatus = SyncStatus.PENDING
                        )
                    )
                }
            }
        }
    }

    override suspend fun holdTransaction(
        cartState: CartState,
        deviceId: String,
        note: String,
        cashierId: String?
    ): Result<TransactionWithDetails> = runCatching {
        if (cartState.items.isEmpty()) {
            throw IllegalArgumentException("Keranjang belanja kosong")
        }

        database.withTransaction {
            val now = System.currentTimeMillis()
            val transactionId = UUID.randomUUID().toString()
            val dateStr = dateOnlyFormat.format(Date(now))
            // Sama seperti checkout: nomor urut dari MAX + 1 di dalam transaksi DB,
            // supaya nomor HOLD tidak pernah menabrak pesanan tertunda yang sudah ada.
            val nextSeq = transactionDao.nextReceiptSequence("HOLD-$deviceId-$dateStr-") + 1
            val receiptNumber = "HOLD-$deviceId-$dateStr-${String.format(Locale.US, "%04d", nextSeq)}"

            val calc = cartState.calculateTotals()

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
                paymentAmount = 0L,
                changeAmount = 0L,
                paymentMethod = PaymentMethod.CASH,
                status = TransactionStatus.HELD,
                note = note.ifBlank { cartState.note },
                createdAt = now,
                syncStatus = SyncStatus.PENDING
            )
            transactionDao.insertTransaction(transactionEntity)

            val itemEntities = mutableListOf<TransactionItemEntity>()
            val modifierEntities = mutableListOf<TransactionItemModifierEntity>()

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
            }

            transactionDao.insertTransactionItems(itemEntities)
            if (modifierEntities.isNotEmpty()) {
                transactionDao.insertTransactionItemModifiers(modifierEntities)
            }

            transactionDao.getTransactionById(transactionId)
                ?: throw IllegalStateException("Gagal memuat pesanan tertunda")
        }
    }

    override suspend fun deleteHeldTransaction(transactionId: String): Result<Unit> = runCatching {
        database.withTransaction {
            transactionDao.deleteTransactionItemModifiers(transactionId)
            transactionDao.deleteTransactionItems(transactionId)
            transactionDao.deleteTransaction(transactionId)
        }
    }

    override fun getGrossSalesBetween(startDate: Long, endDate: Long): Flow<Long?> =
        transactionDao.getGrossSalesBetween(startDate, endDate)

    override fun getTransactionCountBetween(startDate: Long, endDate: Long): Flow<Int> =
        transactionDao.getTransactionCountBetween(startDate, endDate)

    override fun getTopSellingProductsBetween(startDate: Long, endDate: Long, limit: Int): Flow<List<TopSellingProduct>> =
        transactionDao.getTopSellingProductsBetween(startDate, endDate, limit)
}

