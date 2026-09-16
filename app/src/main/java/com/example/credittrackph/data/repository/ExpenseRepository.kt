package com.example.credittrackph.data.repository

import com.example.credittrackph.data.db.dao.ExpenseDao
import com.example.credittrackph.data.db.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    fun getExpensesByCard(cardId: Int): Flow<List<ExpenseEntity>> = expenseDao.getExpensesByCard(cardId)

    fun getExpensesByCardAndMonth(cardId: Int, year: String, month: String): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByCardAndMonth(cardId, year, month)

    fun getUpcomingDueExpenses(startMs: Long, endMs: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getUpcomingDueExpenses(startMs, endMs)

    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    fun getTotalOutstandingByCard(cardId: Int): Flow<Double?> = expenseDao.getTotalOutstandingByCard(cardId)
    fun getTotalOutstanding(): Flow<Double?> = expenseDao.getTotalOutstanding()

    suspend fun insertExpense(expense: ExpenseEntity): Long = expenseDao.insertExpense(expense)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>) = expenseDao.insertExpenses(expenses)
    suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.deleteExpense(expense)
    suspend fun markAsPaid(expenseId: Int) = expenseDao.markAsPaid(expenseId)
}
