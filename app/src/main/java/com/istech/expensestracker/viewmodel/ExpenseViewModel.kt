package com.istech.expensestracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.paging.PagingData
import com.istech.expensestracker.model.Expense
import com.istech.expensestracker.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.MutableLiveData

/**
 * ViewModel for managing expense-related UI data and operations.
 */
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {
    /**
     * Returns a LiveData stream of all expenses, paginated.
     */
    fun getAllExpensesPaged(): LiveData<PagingData<Expense>> =
        repository.getAllExpensesPaged().asLiveData()

    /**
     * Returns a LiveData stream of expenses filtered by category, paginated.
     */
    fun getExpensesByCategoryPaged(category: String): LiveData<PagingData<Expense>> =
        repository.getExpensesByCategoryPaged(category).asLiveData()

    /**
     * Returns a LiveData stream of expenses filtered by date range, paginated.
     */
    fun getExpensesByDatePaged(startDate: Long, endDate: Long): LiveData<PagingData<Expense>> =
        repository.getExpensesByDatePaged(startDate, endDate).asLiveData()

    /**
     * Returns a LiveData stream of expenses filtered by category and date range, paginated.
     */
    fun getExpensesByCategoryAndDatePaged(category: String, startDate: Long, endDate: Long): LiveData<PagingData<Expense>> =
        repository.getExpensesByCategoryAndDatePaged(category, startDate, endDate).asLiveData()

    /**
     * Inserts a new expense into the database.
     */
    fun insertExpense(expense: Expense) {
        viewModelScope.launch { repository.insertExpense(expense) }
    }

    /**
     * Updates an existing expense in the database.
     */
    fun updateExpense(expense: Expense) {
        viewModelScope.launch { repository.updateExpense(expense) }
    }

    /**
     * Deletes an expense from the database.
     */
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    private val _totalExpenses = MutableLiveData<Int>()
    val totalExpenses: LiveData<Int> = _totalExpenses

    /**
     * Loads the total expenses for the given month and year.
     */
    fun loadTotalExpensesForMonth(month: Int, year: Int) {
        viewModelScope.launch {
            _totalExpenses.value = repository.getTotalExpensesForMonth(month.toString().padStart(2, '0'), year.toString())
        }
    }

    /**
     * Gets category totals for the given month and year (for analytics/pie chart).
     */
    suspend fun getCategoryTotalsForMonth(month: Int, year: Int): List<com.istech.expensestracker.data.CategoryTotal> {
        return repository.getCategoryTotalsForMonth(month.toString().padStart(2, '0'), year.toString())
    }
} 