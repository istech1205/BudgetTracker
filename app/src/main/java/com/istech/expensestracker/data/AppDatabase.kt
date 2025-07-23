package com.istech.expensestracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.istech.expensestracker.model.Budget
import com.istech.expensestracker.model.Expense

/**
 * The main Room database for the app, containing Expense and Budget tables.
 */
@Database(
    entities = [Expense::class, Budget::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
} 