package com.example.credittrackph.data.db.dao

import androidx.room.*
import com.example.credittrackph.data.db.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE cardId = :cardId ORDER BY dueDate ASC")
    fun getExpensesByCard(cardId: Int): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT * FROM expenses 
        WHERE cardId = :cardId 
        AND strftime('%Y', datetime(purchaseDate/1000, 'unixepoch')) = :year
        AND strftime('%m', datetime(purchaseDate/1000, 'unixepoch')) = :month
        ORDER BY dueDate ASC
    """)
    fun getExpensesByCardAndMonth(cardId: Int, year: String, month: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE dueDate BETWEEN :startMs AND :endMs AND isPaid = 0 ORDER BY dueDate ASC")
    fun getUpcomingDueExpenses(startMs: Long, endMs: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY dueDate ASC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY dueDate ASC")
    suspend fun getAllExpensesSync(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("UPDATE expenses SET isPaid = 1 WHERE id = :expenseId")
    suspend fun markAsPaid(expenseId: Int)

    @Query("SELECT SUM(monthlyAmortization) FROM expenses WHERE cardId = :cardId AND isPaid = 0")
    fun getTotalOutstandingByCard(cardId: Int): Flow<Double?>

    @Query("SELECT SUM(monthlyAmortization) FROM expenses WHERE isPaid = 0")
    fun getTotalOutstanding(): Flow<Double?>
}
