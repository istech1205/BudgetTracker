package com.istech.expensestracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class representing the monthly budget in the Room database.
 */
@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val month: Int, // 1-12
    val year: Int
) 