package com.rising.pos.data.repository

import com.google.common.truth.Truth.assertThat
import com.rising.pos.core.database.dao.RestaurantTableDao
import com.rising.pos.core.database.entity.RestaurantTableEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TableRepositoryTest {

    private lateinit var restaurantTableDao: RestaurantTableDao
    private lateinit var repository: TableRepositoryImpl

    private val sampleTable = RestaurantTableEntity(
        id = "t1",
        tableNumber = "Meja 01",
        capacity = 4,
        isOccupied = false
    )

    @Before
    fun setUp() {
        restaurantTableDao = mockk(relaxed = true)
        repository = TableRepositoryImpl(restaurantTableDao)
    }

    @Test
    fun `getAllTables returns flow from dao`() = runTest {
        every { restaurantTableDao.getAllTables() } returns flowOf(listOf(sampleTable))

        val result = repository.getAllTables().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].tableNumber).isEqualTo("Meja 01")
        assertThat(result[0].capacity).isEqualTo(4)
        assertThat(result[0].isOccupied).isFalse()
    }

    @Test
    fun `saveTable rejects blank table number`() = runTest {
        val invalidTable = sampleTable.copy(tableNumber = "   ")
        val result = repository.saveTable(invalidTable)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("tidak boleh kosong")
        coVerify(exactly = 0) { restaurantTableDao.insertTable(any()) }
    }

    @Test
    fun `saveTable rejects duplicate table number on different id`() = runTest {
        coEvery { restaurantTableDao.getTableByNumber("Meja 01") } returns sampleTable

        val anotherTableWithSameNumber = RestaurantTableEntity(
            id = "t2",
            tableNumber = "Meja 01",
            capacity = 2
        )
        val result = repository.saveTable(anotherTableWithSameNumber)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("sudah ada")
        coVerify(exactly = 0) { restaurantTableDao.insertTable(any()) }
    }

    @Test
    fun `saveTable inserts valid table and corrects zero capacity`() = runTest {
        coEvery { restaurantTableDao.getTableByNumber("Meja 02") } returns null

        val newTable = RestaurantTableEntity(
            id = "t2",
            tableNumber = "Meja 02",
            capacity = 0
        )
        val result = repository.saveTable(newTable)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) {
            restaurantTableDao.insertTable(match {
                it.tableNumber == "Meja 02" && it.capacity == 4
            })
        }
    }

    @Test
    fun `deleteTable calls dao`() = runTest {
        val result = repository.deleteTable(sampleTable)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { restaurantTableDao.deleteTable(sampleTable) }
    }

    @Test
    fun `updateOccupiedStatus updates status and trims number`() = runTest {
        val result = repository.updateOccupiedStatus(" Meja 01 ", true, "trx-123")

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) {
            restaurantTableDao.updateOccupiedStatusByNumber("Meja 01", true, "trx-123")
        }
    }

    @Test
    fun `seedDefaultTablesIfEmpty seeds default tables when empty`() = runTest {
        coEvery { restaurantTableDao.countTables() } returns 0

        val result = repository.seedDefaultTablesIfEmpty()

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 6) { restaurantTableDao.insertTable(any()) }
    }

    @Test
    fun `seedDefaultTablesIfEmpty does nothing when tables already exist`() = runTest {
        coEvery { restaurantTableDao.countTables() } returns 3

        val result = repository.seedDefaultTablesIfEmpty()

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 0) { restaurantTableDao.insertTable(any()) }
    }

    @Test
    fun `releaseTableByTransactionId calls dao`() = runTest {
        val result = repository.releaseTableByTransactionId("trx-999")

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { restaurantTableDao.releaseTableByTransactionId("trx-999") }
    }
}
