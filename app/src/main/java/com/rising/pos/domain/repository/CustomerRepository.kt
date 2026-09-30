package com.rising.pos.domain.repository

import com.rising.pos.core.database.entity.CustomerEntity
import com.rising.pos.core.database.entity.CustomerWithStats
import com.rising.pos.core.database.entity.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getAllCustomers(): Flow<List<CustomerEntity>>
    fun getAllCustomersWithStats(): Flow<List<CustomerWithStats>>
    fun searchCustomersWithStats(query: String): Flow<List<CustomerWithStats>>
    suspend fun getCustomerById(id: String): CustomerEntity?
    suspend fun saveCustomer(customer: CustomerEntity): Result<Unit>
    suspend fun deleteCustomer(customer: CustomerEntity): Result<Unit>
    fun getCustomerTransactions(customerId: String): Flow<List<TransactionWithDetails>>
}
