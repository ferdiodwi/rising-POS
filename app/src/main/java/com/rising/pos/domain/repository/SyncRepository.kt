package com.rising.pos.domain.repository

import com.rising.pos.core.database.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

data class SyncResult(
    val success: Boolean,
    val syncedCount: Int,
    val message: String
)

interface SyncRepository {
    fun getPendingCount(): Flow<Int>
    fun getAllSyncQueue(): Flow<List<SyncQueueEntity>>
    suspend fun enqueue(
        entityType: String,
        entityId: String,
        action: String,
        payloadJson: String,
        deviceId: String = "DEVICE-LOCAL"
    )
    suspend fun syncNow(): Result<SyncResult>
    suspend fun clearSynced()
    fun schedulePeriodicSync()
}
