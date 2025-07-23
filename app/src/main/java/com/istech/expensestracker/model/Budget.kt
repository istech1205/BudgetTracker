package com.istech.expensestracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity class representing the monthly budget in the Room database.
 */
@Entity(tableName = "budgets", indices = [Index(value = ["month", "year"], unique = true)])
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Int,
    val month: Int, // 1-12
    val year: Int
) 