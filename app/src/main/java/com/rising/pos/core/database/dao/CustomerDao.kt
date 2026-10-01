package com.rising.pos.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rising.pos.core.database.entity.CustomerEntity
import com.rising.pos.core.database.entity.CustomerWithStats
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("""
        SELECT 
            c.*,
            COUNT(t.id) AS total_transactions,
            COALESCE(SUM(CASE WHEN t.status = 'COMPLETED' THEN t.grand_total ELSE 0 END), 0) AS total_spent,
            MAX(t.created_at) AS last_transaction_date
        FROM customers c
        LEFT JOIN transactions t ON c.id = t.customer_id
        GROUP BY c.id
        ORDER BY c.name ASC
    """)
    fun getAllCustomersWithStats(): Flow<List<CustomerWithStats>>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("""
        SELECT 
            c.*,
            COUNT(t.id) AS total_transactions,
            COALESCE(SUM(CASE WHEN t.status = 'COMPLETED' THEN t.grand_total ELSE 0 END), 0) AS total_spent,
            MAX(t.created_at) AS last_transaction_date
        FROM customers c
        LEFT JOIN transactions t ON c.id = t.customer_id
        WHERE c.name LIKE '%' || :query || '%' OR c.phone LIKE '%' || :query || '%'
        GROUP BY c.id
        ORDER BY c.name ASC
    """)
    fun searchCustomersWithStats(query: String): Flow<List<CustomerWithStats>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)
}
