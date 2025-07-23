package com.istech.expensestracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class representing a single expense record in the Room database.
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val category: String,
    val date: Long, // Store as epoch millis for easy sorting/filtering
    val note: String? = null
) 