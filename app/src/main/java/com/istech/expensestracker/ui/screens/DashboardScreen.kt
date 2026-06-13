package com.istech.expensestracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.istech.expensestracker.ui.components.BarChart
import com.istech.expensestracker.ui.components.BarChartEntry
import com.istech.expensestracker.ui.components.Header
import com.istech.expensestracker.ui.components.StatCard
import com.istech.expensestracker.ui.theme.DarkGreen
import com.istech.expensestracker.ui.theme.DarkRed
import com.istech.expensestracker.ui.theme.LowGreen
import com.istech.expensestracker.ui.theme.LightPrimary
import com.istech.expensestracker.ui.theme.LightRed
import com.istech.expensestracker.ui.theme.PrimaryDark
import com.istech.expensestracker.viewmodel.BudgetViewModel
import com.istech.expensestracker.viewmodel.DashboardViewModel
import com.istech.expensestracker.viewmodel.ExpenseViewModel

@Composable
fun DashboardScreen(
    onAddExpense: () -> Unit,
    onSetBudget: () -> Unit,
    onViewHistory: () -> Unit,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by dashboardViewModel.uiState.observeAsState()
    val budget by budgetViewModel.budget.observeAsState()
    val totalExpenses by expenseViewModel.totalExpenses.observeAsState()
    var showMonthPicker by remember { mutableStateOf(false) }

    // Load data when month/year changes
    LaunchedEffect(uiState?.selectedMonth, uiState?.selectedYear) {
        val month = uiState?.selectedMonth ?: return@LaunchedEffect
        val year = uiState?.selectedYear ?: return@LaunchedEffect
        budgetViewModel.loadBudget(month, year)
        expenseViewModel.loadTotalExpensesForMonth(month, year)
        dashboardViewModel.loadCategoryTotals()
    }

    val state = uiState ?: return

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Header(title = "Dashboard")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Month Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMonthPicker = true },
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = PrimaryDark,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = dashboardViewModel.monthYearDisplay,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bar Chart
            val barChartEntries = state.categoryTotals.map {
                BarChartEntry(it.category, it.total.toFloat())
            }

            BarChart(
                entries = barChartEntries,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatCard(
                    title = "Monthly\nBudget",
                    value = "₹${budget?.amount ?: 0}",
                    containerColor = LightPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Total\nExpenses",
                    value = "₹${totalExpenses ?: 0}",
                    containerColor = LightRed,
                    contentColor = DarkRed,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Remaining\nBalance",
                    value = "₹${(budget?.amount ?: 0) - (totalExpenses ?: 0)}",
                    containerColor = LowGreen,
                    contentColor = DarkGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Action Buttons
            Button(
                onClick = onAddExpense,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Add Expense")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSetBudget,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkRed)
            ) {
                Icon(
                    imageVector = Icons.Default.Wallet,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Set Budget")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onViewHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Expense History")
            }
        }
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialMonth = state.selectedMonth,
            initialYear = state.selectedYear,
            onDismiss = { showMonthPicker = false },
            onConfirm = { month, year ->
                dashboardViewModel.setMonthYear(month, year)
                showMonthPicker = false
            }
        )
    }
}
