package com.rising.pos.data.repository

import com.google.common.truth.Truth.assertThat
import com.rising.pos.core.database.dao.CustomerDao
import com.rising.pos.core.database.dao.TransactionDao
import com.rising.pos.core.database.entity.CustomerEntity
import com.rising.pos.core.database.entity.CustomerWithStats
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CustomerRepositoryTest {

    private lateinit var customerDao: CustomerDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var repository: CustomerRepositoryImpl

    private val sampleCustomer = CustomerEntity(
        id = "c1",
        name = "Budi Santoso",
        phone = "08123456789",
        email = "budi@example.com",
        address = "Jl. Merdeka No. 1"
    )

    private val sampleCustomerWithStats = CustomerWithStats(
        customer = sampleCustomer,
        totalTransactions = 3,
        totalSpent = 150000L,
        lastTransactionDate = 1759230000000L
    )

    @Before
    fun setUp() {
        customerDao = mockk(relaxed = true)
        transactionDao = mockk(relaxed = true)
        repository = CustomerRepositoryImpl(customerDao, transactionDao)
    }

    @Test
    fun `getAllCustomersWithStats returns flow from dao`() = runTest {
        every { customerDao.getAllCustomersWithStats() } returns flowOf(listOf(sampleCustomerWithStats))

        val result = repository.getAllCustomersWithStats().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].customer.name).isEqualTo("Budi Santoso")
        assertThat(result[0].totalTransactions).isEqualTo(3)
        assertThat(result[0].totalSpent).isEqualTo(150000L)
    }

    @Test
    fun `searchCustomersWithStats queries dao with pattern`() = runTest {
        every { customerDao.searchCustomersWithStats("Budi") } returns flowOf(listOf(sampleCustomerWithStats))

        val result = repository.searchCustomersWithStats("Budi").first()

        assertThat(result).hasSize(1)
        assertThat(result[0].customer.id).isEqualTo("c1")
    }

    @Test
    fun `saveCustomer inserts into dao successfully`() = runTest {
        coEvery { customerDao.insertCustomer(sampleCustomer) } returns Unit

        val result = repository.saveCustomer(sampleCustomer)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { customerDao.insertCustomer(sampleCustomer) }
    }

    @Test
    fun `deleteCustomer removes from dao successfully`() = runTest {
        coEvery { customerDao.deleteCustomer(sampleCustomer) } returns Unit

        val result = repository.deleteCustomer(sampleCustomer)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { customerDao.deleteCustomer(sampleCustomer) }
    }
}
