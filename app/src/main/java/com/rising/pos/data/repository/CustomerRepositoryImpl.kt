package com.rising.pos.data.repository

import com.rising.pos.core.database.dao.CustomerDao
import com.rising.pos.core.database.dao.TransactionDao
import com.rising.pos.core.database.entity.CustomerEntity
import com.rising.pos.core.database.entity.CustomerWithStats
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao
) : CustomerRepository {

    override fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    override fun getAllCustomersWithStats(): Flow<List<CustomerWithStats>> =
        customerDao.getAllCustomersWithStats()

    override fun searchCustomersWithStats(query: String): Flow<List<CustomerWithStats>> =
        customerDao.searchCustomersWithStats(query)

    override suspend fun getCustomerById(id: String): CustomerEntity? =
        customerDao.getCustomerById(id)

    override suspend fun saveCustomer(customer: CustomerEntity): Result<Unit> = runCatching {
        customerDao.insertCustomer(customer)
    }

    override suspend fun deleteCustomer(customer: CustomerEntity): Result<Unit> = runCatching {
        customerDao.deleteCustomer(customer)
    }

    override fun getCustomerTransactions(customerId: String): Flow<List<TransactionWithDetails>> =
        transactionDao.getTransactionsByCustomerId(customerId)
}
