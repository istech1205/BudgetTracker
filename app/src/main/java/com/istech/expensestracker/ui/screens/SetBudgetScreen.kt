package com.istech.expensestracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.istech.expensestracker.model.Budget
import com.istech.expensestracker.ui.components.AmountField
import com.istech.expensestracker.ui.components.CategoryDropdown
import com.istech.expensestracker.ui.components.FormSectionSpacing
import com.istech.expensestracker.ui.components.Header
import com.istech.expensestracker.viewmodel.BudgetViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBudgetScreen(
    onBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var year by remember { mutableStateOf(calendar.get(Calendar.YEAR).toString()) }
    var amount by remember { mutableStateOf("") }

    val months = remember {
        listOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")
    }

    val budget by viewModel.budget.observeAsState()

    // Load budget when month/year changes
    LaunchedEffect(selectedMonth, year) {
        val yearInt = year.toIntOrNull() ?: return@LaunchedEffect
        viewModel.loadBudget(selectedMonth + 1, yearInt)
    }

    // Prefill amount when budget loads
    LaunchedEffect(budget) {
        amount = budget?.amount?.toString() ?: ""
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Header(
            title = "Set Monthly Budget",
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
                label = "Budget Amount",
                maxLength = 6,
                imeAction = ImeAction.Next
            )

            FormSectionSpacing()

            // Month Dropdown
            CategoryDropdown(
                selectedCategory = months[selectedMonth],
                onCategorySelected = { selectedMonth = months.indexOf(it) },
                categories = months,
                label = "Month"
            )

            FormSectionSpacing()

            // Year
            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Year") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    val amountValue = amount.toIntOrNull()
                    val yearValue = year.toIntOrNull()

                    if (amountValue == null || yearValue == null) {
                        Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (amountValue < 1000) {
                        Toast.makeText(context, "Budget should not be less than 1000", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val budget = Budget(
                        amount = amountValue,
                        month = selectedMonth + 1,
                        year = yearValue
                    )
                    viewModel.insertOrUpdateBudget(budget)
                    Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Budget")
            }
        }
    }
}
