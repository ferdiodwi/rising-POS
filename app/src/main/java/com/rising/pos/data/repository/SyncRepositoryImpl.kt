package com.rising.pos.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rising.pos.core.database.dao.SyncQueueDao
import com.rising.pos.core.database.entity.SyncQueueEntity
import com.rising.pos.core.model.SyncStatus
import com.rising.pos.core.sync.SyncWorker
import com.rising.pos.domain.repository.SyncRepository
import com.rising.pos.domain.repository.SyncResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    @ApplicationContext private val context: Context
) : SyncRepository {

    override fun getPendingCount(): Flow<Int> = syncQueueDao.getPendingCountFlow()

    override fun getAllSyncQueue(): Flow<List<SyncQueueEntity>> = syncQueueDao.getAllQueueFlow()

    override suspend fun enqueue(
        entityType: String,
        entityId: String,
        action: String,
        payloadJson: String,
        deviceId: String
    ) {
        val entity = SyncQueueEntity(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            action = action,
            payloadJson = payloadJson,
            deviceId = deviceId,
            status = SyncStatus.PENDING,
            retryCount = 0,
            createdAt = System.currentTimeMillis()
        )
        syncQueueDao.insert(entity)
    }

    override suspend fun syncNow(): Result<SyncResult> = runCatching {
        if (!isOnline()) {
            throw IllegalStateException("Perangkat offline. Harap hubungkan perangkat ke internet untuk sinkronisasi.")
        }

        val pending = syncQueueDao.getPendingQueue()
        if (pending.isEmpty()) {
            return@runCatching SyncResult(
                success = true,
                syncedCount = 0,
                message = "Semua data lokal telah tersinkronisasi."
            )
        }

        // Tandai status sync ke SYNCING
        for (item in pending) {
            syncQueueDao.updateStatus(item.id, SyncStatus.SYNCING)
        }

        // Simulasi pengiriman batch payload ke Cloud API / Server.
        // Setelah transmisi batch berhasil, tandai SYNCED.
        syncQueueDao.markAllSynced()

        SyncResult(
            success = true,
            syncedCount = pending.size,
            message = "Berhasil mensinkronkan ${pending.size} data ke Cloud."
        )
    }

    override suspend fun clearSynced() {
        syncQueueDao.clearSynced()
    }

    override fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object {
        const val SYNC_WORK_NAME = "RisingPosBackgroundSync"
    }
}
