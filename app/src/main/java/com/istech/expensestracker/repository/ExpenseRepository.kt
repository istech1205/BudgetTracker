package com.istech.expensestracker.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.istech.expensestracker.data.ExpenseDao
import com.istech.expensestracker.data.CategoryTotal
import com.istech.expensestracker.model.Expense
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository for managing expense data operations.
 */
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    fun getAllExpensesPaged(pageSize: Int = 20): Flow<PagingData<Expense>> =
        Pager(PagingConfig(pageSize = pageSize)) {
            expenseDao.getAllExpensesPaged()
        }.flow

    fun getExpensesByCategoryPaged(category: String, pageSize: Int = 20): Flow<PagingData<Expense>> =
        Pager(PagingConfig(pageSize = pageSize)) {
            expenseDao.getExpensesByCategoryPaged(category)
        }.flow

    fun getExpensesByDatePaged(startDate: Long, endDate: Long, pageSize: Int = 20): Flow<PagingData<Expense>> =
        Pager(PagingConfig(pageSize = pageSize)) {
            expenseDao.getExpensesByDatePaged(startDate, endDate)
        }.flow

    fun getExpensesByCategoryAndDatePaged(category: String, startDate: Long, endDate: Long, pageSize: Int = 20): Flow<PagingData<Expense>> =
        Pager(PagingConfig(pageSize = pageSize)) {
            expenseDao.getExpensesByCategoryAndDatePaged(category, startDate, endDate)
        }.flow

    suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
    suspend fun getTotalExpensesForMonth(month: String, year: String): Int =
        expenseDao.getTotalExpensesForMonth(month, year) ?: 0
    suspend fun getCategoryTotalsForMonth(month: String, year: String): List<CategoryTotal> =
        expenseDao.getCategoryTotalsForMonth(month, year)
} 