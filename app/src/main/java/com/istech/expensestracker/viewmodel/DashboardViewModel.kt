package com.istech.expensestracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.istech.expensestracker.data.CategoryTotal
import com.istech.expensestracker.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class DashboardUiState(
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(DashboardUiState())
    val uiState: LiveData<DashboardUiState> = _uiState

    val monthYearDisplay: String
        get() {
            val state = _uiState.value ?: return ""
            val cal = Calendar.getInstance()
            cal.set(Calendar.MONTH, state.selectedMonth - 1)
            val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
            return "$monthName ${state.selectedYear}"
        }

    init {
        loadCategoryTotals()
    }

    fun setMonthYear(month: Int, year: Int) {
        _uiState.value = _uiState.value?.copy(
            selectedMonth = month,
            selectedYear = year
        )
        loadCategoryTotals()
    }

    fun loadCategoryTotals() {
        val state = _uiState.value ?: return
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            val totals = expenseRepository.getCategoryTotalsForMonth(
                state.selectedMonth.toString().padStart(2, '0'),
                state.selectedYear.toString()
            )
            _uiState.value = state.copy(
                categoryTotals = totals,
                isLoading = false
            )
        }
    }
}
