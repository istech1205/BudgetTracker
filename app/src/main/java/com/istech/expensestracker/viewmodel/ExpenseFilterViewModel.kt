package com.istech.expensestracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.paging.PagingData
import com.istech.expensestracker.model.Expense
import com.istech.expensestracker.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject

data class FilterState(
    val startDate: Long,
    val endDate: Long,
    val selectedCategory: String? = null,
    val isMonthDefault: Boolean = true
)

@HiltViewModel
class ExpenseFilterViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    companion object {
        val CATEGORIES = listOf(
            "Food", "Transport", "Shopping", "Utilities", 
            "Health", "Entertainment", "Other"
        )
    }

    private val _filterState = MutableLiveData(createDefaultMonthFilter())
    val filterState: LiveData<FilterState> = _filterState

    private val _dateDisplay = MutableLiveData<String>()
    val dateDisplay: LiveData<String> = _dateDisplay

    init {
        updateDateDisplay()
    }

    private fun createDefaultMonthFilter(): FilterState {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        
        return FilterState(startDate = start, endDate = end)
    }

    fun getExpensesFlow(): kotlinx.coroutines.flow.Flow<PagingData<Expense>> {
        val state = _filterState.value ?: return kotlinx.coroutines.flow.emptyFlow()
        return when {
            state.selectedCategory != null ->
                expenseRepository.getExpensesByCategoryAndDatePaged(
                    state.selectedCategory, state.startDate, state.endDate
                )
            else ->
                expenseRepository.getExpensesByDatePaged(
                    state.startDate, state.endDate
                )
        }
    }

    fun setCategory(category: String?) {
        _filterState.value = _filterState.value?.copy(selectedCategory = category)
    }

    fun setDateRange(startDate: Long, endDate: Long, isMonthDefault: Boolean = false) {
        _filterState.value = _filterState.value?.copy(
            startDate = startDate,
            endDate = endDate,
            isMonthDefault = isMonthDefault
        )
        updateDateDisplay()
    }

    fun resetToCurrentMonth() {
        _filterState.value = createDefaultMonthFilter()
        updateDateDisplay()
    }

    fun toggleDateFilter() {
        val state = _filterState.value ?: return
        if (!state.isMonthDefault) {
            resetToCurrentMonth()
        }
    }

    private fun updateDateDisplay() {
        val state = _filterState.value ?: return
        val formatter = if (state.isMonthDefault) {
            // Show month/year for default month filter
            java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
        } else {
            // Show full date for specific day filter
            java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        }
        _dateDisplay.value = formatter.format(java.util.Date(state.startDate))
    }

    fun getFormattedDate(): String {
        val state = _filterState.value ?: return ""
        val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        return formatter.format(java.util.Date(state.startDate))
    }
}
