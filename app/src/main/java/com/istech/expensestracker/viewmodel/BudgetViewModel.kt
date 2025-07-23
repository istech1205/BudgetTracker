package com.istech.expensestracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.istech.expensestracker.model.Budget
import com.istech.expensestracker.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing budget-related UI data and operations.
 */
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {
    private val _budget = MutableLiveData<Budget?>()
    val budget: LiveData<Budget?> = _budget

    /**
     * Loads the budget for the given month and year.
     */
    fun loadBudget(month: Int, year: Int) {
        viewModelScope.launch {
            _budget.value = repository.getBudgetForMonth(month, year)
        }
    }

    /**
     * Inserts or updates the monthly budget in the database.
     */
    fun insertOrUpdateBudget(budget: Budget) {
        viewModelScope.launch {
            repository.insertOrUpdateBudget(budget)
            loadBudget(budget.month, budget.year)
        }
    }
} 