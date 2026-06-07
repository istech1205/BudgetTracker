package com.istech.expensestracker.data

import androidx.paging.PagingSource
import androidx.room.*
import com.istech.expensestracker.model.Expense

/**
 * DAO for accessing Expense data in the Room database.
 */
@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpensesPaged(): PagingSource<Int, Expense>

    @Query("SELECT * FROM expenses WHERE category = :category  ORDER BY date DESC")
    fun getExpensesByCategoryPaged(category: String): PagingSource<Int, Expense>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDatePaged(startDate: Long, endDate: Long): PagingSource<Int, Expense>

    @Query("SELECT * FROM expenses WHERE category = :category AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByCategoryAndDatePaged(category: String, startDate: Long, endDate: Long): PagingSource<Int, Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE strftime('%m', date / 1000, 'unixepoch') = :month AND strftime('%Y', date / 1000, 'unixepoch') = :year")
    suspend fun getTotalExpensesForMonth(month: String, year: String): Int?

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE strftime('%m', date / 1000, 'unixepoch') = :month AND strftime('%Y', date / 1000, 'unixepoch') = :year GROUP BY category")
    suspend fun getCategoryTotalsForMonth(month: String, year: String): List<CategoryTotal>

    @Query("SELECT SUM(amount) FROM expenses")
    suspend fun getTotalExpensesAll(): Int?

    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category")
    suspend fun getTotalExpensesByCategory(category: String): Int?

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalExpensesByDate(startDate: Long, endDate: Long): Int?

    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalExpensesByCategoryAndDate(category: String, startDate: Long, endDate: Long): Int?
}

/**
 * Data class for category total aggregation.
 */
data class CategoryTotal(
    val category: String,
    val total: Int
) 