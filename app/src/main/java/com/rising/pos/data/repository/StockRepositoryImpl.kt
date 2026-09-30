package com.rising.pos.data.repository

import androidx.room.withTransaction
import com.rising.pos.core.database.PosDatabase
import com.rising.pos.core.database.dao.ProductDao
import com.rising.pos.core.database.dao.StockMovementDao
import com.rising.pos.core.database.entity.StockMovementEntity
import com.rising.pos.core.model.StockMovementType
import com.rising.pos.core.model.SyncStatus
import com.rising.pos.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val database: PosDatabase,
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao
) : StockRepository {

    override suspend fun recordRestock(productId: String, qtyAdded: Double, reason: String?) {
        if (qtyAdded <= 0) return
        database.withTransaction {
            val product = productDao.getProductById(productId) ?: return@withTransaction
            val before = product.stock
            val after = before + qtyAdded
            val now = System.currentTimeMillis()

            productDao.updateStock(productId, after, now)
            stockMovementDao.insertMovement(
                StockMovementEntity(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    type = StockMovementType.IN,
                    qtyChange = qtyAdded,
                    qtyBefore = before,
                    qtyAfter = after,
                    reason = reason ?: "Stok Masuk / Kulakan",
                    createdAt = now,
                    syncStatus = SyncStatus.PENDING
                )
            )
        }
    }

    override suspend fun recordAdjustment(productId: String, newActualStock: Double, reason: String?) {
        database.withTransaction {
            val product = productDao.getProductById(productId) ?: return@withTransaction
            val before = product.stock
            val delta = newActualStock - before
            val now = System.currentTimeMillis()

            productDao.updateStock(productId, newActualStock, now)
            stockMovementDao.insertMovement(
                StockMovementEntity(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    type = StockMovementType.ADJUSTMENT,
                    qtyChange = delta,
                    qtyBefore = before,
                    qtyAfter = newActualStock,
                    reason = reason ?: "Penyesuaian Fisik (Stock Opname)",
                    createdAt = now,
                    syncStatus = SyncStatus.PENDING
                )
            )
        }
    }

    override suspend fun recordDamageOrLoss(productId: String, qtyLost: Double, isLoss: Boolean, reason: String?) {
        if (qtyLost <= 0) return
        database.withTransaction {
            val product = productDao.getProductById(productId) ?: return@withTransaction
            val before = product.stock
            val after = maxOf(0.0, before - qtyLost)
            val now = System.currentTimeMillis()

            productDao.updateStock(productId, after, now)
            stockMovementDao.insertMovement(
                StockMovementEntity(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    type = if (isLoss) StockMovementType.LOSS else StockMovementType.DAMAGE,
                    qtyChange = -qtyLost,
                    qtyBefore = before,
                    qtyAfter = after,
                    reason = reason ?: if (isLoss) "Stok Hilang" else "Stok Rusak / Basi",
                    createdAt = now,
                    syncStatus = SyncStatus.PENDING
                )
            )
        }
    }

    override fun getMovementsForProduct(productId: String): Flow<List<StockMovementEntity>> =
        stockMovementDao.getMovementsForProduct(productId)

    override fun getRecentMovements(limit: Int): Flow<List<StockMovementEntity>> =
        stockMovementDao.getRecentMovements(limit)
}
