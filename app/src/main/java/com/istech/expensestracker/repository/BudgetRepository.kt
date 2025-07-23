package com.istech.expensestracker.repository

import com.istech.expensestracker.data.BudgetDao
import com.istech.expensestracker.model.Budget
import javax.inject.Inject

/**
 * Repository for managing budget data operations.
 */
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    suspend fun insertOrUpdateBudget(budget: Budget) = budgetDao.insertOrUpdateBudget(budget)
    suspend fun getBudgetForMonth(month: Int, year: Int): Budget? = budgetDao.getBudgetForMonth(month, year)
} 