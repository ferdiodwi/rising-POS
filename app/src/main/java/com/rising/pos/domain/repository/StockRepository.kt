package com.rising.pos.domain.repository

import com.rising.pos.core.database.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    suspend fun recordRestock(productId: String, qtyAdded: Double, reason: String? = null)
    suspend fun recordAdjustment(productId: String, newActualStock: Double, reason: String? = null)
    suspend fun recordDamageOrLoss(productId: String, qtyLost: Double, isLoss: Boolean, reason: String? = null)
    fun getMovementsForProduct(productId: String): Flow<List<StockMovementEntity>>
    fun getRecentMovements(limit: Int = 100): Flow<List<StockMovementEntity>>
}
