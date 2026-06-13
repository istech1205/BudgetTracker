package com.istech.expensestracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.istech.expensestracker.model.Expense
import com.istech.expensestracker.ui.components.ExpenseItem
import com.istech.expensestracker.ui.components.Header
import com.istech.expensestracker.ui.components.OptionalCategoryDropdown
import com.istech.expensestracker.viewmodel.ExpenseFilterViewModel
import com.istech.expensestracker.viewmodel.ExpenseViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onBack: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel(),
    filterViewModel: ExpenseFilterViewModel = hiltViewModel()
) {
    val calendar = Calendar.getInstance()
    
    val filterState by filterViewModel.filterState.observeAsState()
    val dateDisplay by filterViewModel.dateDisplay.observeAsState()
    
    // Get paging data as Flow and collect as lazy items
    val expensesFlow = remember(filterState) {
        filterViewModel.getExpensesFlow()
    }
    val expensesPaged = expensesFlow.collectAsLazyPagingItems()
    
    var showUpdateDialog by remember { mutableStateOf<Expense?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Expense?>(null) }

    val categories = remember { ExpenseFilterViewModel.CATEGORIES }
    
    // Load total when filters change
    LaunchedEffect(filterState?.selectedCategory, filterState?.startDate, filterState?.endDate) {
        val state = filterState ?: return@LaunchedEffect
        viewModel.loadFilteredTotal(state.selectedCategory, state.startDate, state.endDate)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Header(
            title = "Expense History",
            onBackClick = onBack,
            trailingContent = {
                Text(
                    text = "₹${viewModel.filteredTotal.value ?: 0}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        )

        // Filter Row
        val state = filterState ?: return
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Dropdown
            OptionalCategoryDropdown(
                selectedCategory = state.selectedCategory,
                onCategorySelected = { filterViewModel.setCategory(it) },
                categories = categories,
                label = "Category",
                modifier = Modifier.weight(1f)
            )

            // Date Filter Button
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    val cal = Calendar.getInstance()
                    android.app.DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val newCal = Calendar.getInstance()
                            newCal.set(year, month, dayOfMonth, 0, 0, 0)
                            newCal.set(Calendar.MILLISECOND, 0)
                            val start = newCal.timeInMillis
                            newCal.set(Calendar.HOUR_OF_DAY, 23)
                            newCal.set(Calendar.MINUTE, 59)
                            newCal.set(Calendar.SECOND, 59)
                            newCal.set(Calendar.MILLISECOND, 999)
                            val end = newCal.timeInMillis
                            filterViewModel.setDateRange(start, end)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            ) {
                Text(dateDisplay ?: "Date")
            }
        }

        // Expense List
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (expensesPaged.loadState.refresh) {
                is LoadState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is LoadState.Error -> {
                    Text(
                        text = "Error loading expenses",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    if (expensesPaged.itemCount == 0) {
                        Text(
                            text = "No expenses found",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                count = expensesPaged.itemCount,
                                key = { index: Int -> expensesPaged.peek(index)?.id ?: index },
                                contentType = { "Expense" }
                            ) { index ->
                                val expense = expensesPaged[index]
                                if (expense != null) {
                                    ExpenseItem(
                                        expense = expense,
                                        onUpdate = { showUpdateDialog = expense },
                                        onDelete = { showDeleteDialog = expense }
                                    )
                                }
                            }

                            item {
                                if (expensesPaged.loadState.append is LoadState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { expense ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(expense)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("No")
                }
            }
        )
    }

    // Update Dialog
    showUpdateDialog?.let { expense ->
        UpdateExpenseDialog(
            expense = expense,
            onDismiss = { showUpdateDialog = null },
            onConfirm = { updatedExpense ->
                viewModel.updateExpense(updatedExpense)
                showUpdateDialog = null
            }
        )
    }
}
