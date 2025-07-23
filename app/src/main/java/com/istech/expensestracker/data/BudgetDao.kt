package com.istech.expensestracker.data

import androidx.room.*
import com.istech.expensestracker.model.Budget

/**
 * DAO for accessing Budget data in the Room database.
 */
@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year LIMIT 1")
    suspend fun getBudgetForMonth(month: Int, year: Int): Budget?
} 