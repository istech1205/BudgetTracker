package com.istech.expensestracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.istech.expensestracker.model.Expense
import com.istech.expensestracker.ui.components.AmountField
import com.istech.expensestracker.ui.components.CategoryDropdown
import com.istech.expensestracker.ui.components.DatePickerField
import com.istech.expensestracker.ui.components.FormSectionSpacing
import com.istech.expensestracker.ui.components.Header
import com.istech.expensestracker.ui.components.NoteField
import com.istech.expensestracker.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBack: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var note by remember { mutableStateOf("") }

    val categories = remember { 
        listOf("Food", "Transport", "Shopping", "Utilities", "Health", "Entertainment", "Other")
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Header(
            title = "Add Expense",
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Amount
            AmountField(
                value = amount,
                onValueChange = { amount = it },
                label = "Amount",
                maxLength = 7
            )

            FormSectionSpacing()

            // Category Dropdown
            CategoryDropdown(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                categories = categories,
                label = "Category"
            )

            FormSectionSpacing()

            // Date Picker
            DatePickerField(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                label = "Date",
                maxDate = System.currentTimeMillis()
            )

            FormSectionSpacing()

            // Note
            NoteField(
                value = note,
                onValueChange = { note = it },
                label = "Note (Optional)",
                maxLength = 30
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    val amountValue = amount.toIntOrNull()
                    if (amountValue != null && amountValue > 0) {
                        val expense = Expense(
                            amount = amountValue,
                            category = selectedCategory,
                            date = selectedDate,
                            note = note.takeIf { it.isNotBlank() }
                        )
                        viewModel.insertExpense(expense)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Expense")
            }
        }
    }
}
