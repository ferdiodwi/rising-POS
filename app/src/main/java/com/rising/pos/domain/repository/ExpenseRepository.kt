package com.rising.pos.domain.repository

import com.rising.pos.core.database.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    fun getExpensesBetween(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>
    fun getTotalExpenseBetween(startDate: Long, endDate: Long): Flow<Double?>
    suspend fun saveExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(expense: ExpenseEntity)
}
