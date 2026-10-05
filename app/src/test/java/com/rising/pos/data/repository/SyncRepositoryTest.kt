package com.rising.pos.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.google.common.truth.Truth.assertThat
import com.rising.pos.core.database.dao.SyncQueueDao
import com.rising.pos.core.database.entity.SyncQueueEntity
import com.rising.pos.core.model.SyncStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SyncRepositoryTest {

    private lateinit var syncQueueDao: SyncQueueDao
    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var network: Network
    private lateinit var networkCapabilities: NetworkCapabilities
    private lateinit var repository: SyncRepositoryImpl

    private val sampleSyncItem = SyncQueueEntity(
        id = "sync-1",
        entityType = "TRANSACTION",
        entityId = "trx-101",
        action = "INSERT",
        payloadJson = """{"id":"trx-101"}""",
        deviceId = "DEVICE-01",
        status = SyncStatus.PENDING,
        retryCount = 0,
        createdAt = System.currentTimeMillis()
    )

    @Before
    fun setUp() {
        syncQueueDao = mockk(relaxed = true)
        context = mockk(relaxed = true)
        connectivityManager = mockk(relaxed = true)
        network = mockk(relaxed = true)
        networkCapabilities = mockk(relaxed = true)

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        repository = SyncRepositoryImpl(syncQueueDao, context)
    }

    @Test
    fun `getPendingCount returns flow from dao`() = runTest {
        every { syncQueueDao.getPendingCountFlow() } returns flowOf(5)

        val result = repository.getPendingCount().first()

        assertThat(result).isEqualTo(5)
    }

    @Test
    fun `enqueue inserts new SyncQueueEntity with PENDING status`() = runTest {
        repository.enqueue(
            entityType = "TRANSACTION",
            entityId = "trx-101",
            action = "INSERT",
            payloadJson = """{"total":50000}""",
            deviceId = "DEV-01"
        )

        coVerify(exactly = 1) {
            syncQueueDao.insert(match {
                it.entityType == "TRANSACTION" &&
                        it.entityId == "trx-101" &&
                        it.action == "INSERT" &&
                        it.status == SyncStatus.PENDING
            })
        }
    }

    @Test
    fun `syncNow fails when device is offline`() = runTest {
        every { connectivityManager.activeNetwork } returns null

        val result = repository.syncNow()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("offline")
        coVerify(exactly = 0) { syncQueueDao.markAllSynced() }
    }

    @Test
    fun `syncNow succeeds with 0 count when queue is empty`() = runTest {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        coEvery { syncQueueDao.getPendingQueue() } returns emptyList()

        val result = repository.syncNow()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.syncedCount).isEqualTo(0)
        coVerify(exactly = 0) { syncQueueDao.markAllSynced() }
    }

    @Test
    fun `syncNow processes pending items and marks all synced when online`() = runTest {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        coEvery { syncQueueDao.getPendingQueue() } returns listOf(sampleSyncItem)

        val result = repository.syncNow()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.syncedCount).isEqualTo(1)
        coVerify(exactly = 1) { syncQueueDao.updateStatus("sync-1", SyncStatus.SYNCING) }
        coVerify(exactly = 1) { syncQueueDao.markAllSynced() }
    }

    @Test
    fun `clearSynced delegates to dao`() = runTest {
        repository.clearSynced()

        coVerify(exactly = 1) { syncQueueDao.clearSynced() }
    }
}
