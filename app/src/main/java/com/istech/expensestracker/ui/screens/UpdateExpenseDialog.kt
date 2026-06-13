package com.istech.expensestracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.istech.expensestracker.model.Expense
import com.istech.expensestracker.ui.components.AmountField
import com.istech.expensestracker.ui.components.CategoryDropdown
import com.istech.expensestracker.ui.components.DatePickerField
import com.istech.expensestracker.ui.components.FormSectionSpacing
import com.istech.expensestracker.ui.components.NoteField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateExpenseDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    onConfirm: (Expense) -> Unit
) {
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var selectedDate by remember { mutableLongStateOf(expense.date) }
    var note by remember { mutableStateOf(expense.note ?: "") }

    val categories = remember { 
        listOf("Food", "Transport", "Shopping", "Utilities", "Health", "Entertainment", "Other")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Expense") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Amount
                AmountField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Amount"
                )

                FormSectionSpacing(height = 12.dp)

                // Category Dropdown
                CategoryDropdown(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    categories = categories,
                    label = "Category"
                )

                FormSectionSpacing(height = 12.dp)

                // Date
                DatePickerField(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    label = "Date",
                    maxDate = System.currentTimeMillis()
                )

                FormSectionSpacing(height = 12.dp)

                // Note
                NoteField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Note"
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toIntOrNull()
                    if (amountValue != null) {
                        val updatedExpense = expense.copy(
                            amount = amountValue,
                            category = selectedCategory,
                            date = selectedDate,
                            note = note.takeIf { it.isNotBlank() }
                        )
                        onConfirm(updatedExpense)
                    }
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
