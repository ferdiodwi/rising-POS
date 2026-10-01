package com.rising.pos.data.repository

import com.rising.pos.core.database.dao.ExpenseDao
import com.rising.pos.core.database.entity.ExpenseEntity
import com.rising.pos.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    override fun getExpensesBetween(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesBetween(startDate, endDate)

    override fun getTotalExpenseBetween(startDate: Long, endDate: Long): Flow<Long?> =
        expenseDao.getTotalExpenseBetween(startDate, endDate)

    override suspend fun saveExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    override suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }
}
