package com.istech.expensestracker.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    categories: List<String>,
    label: String = "Category",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun OptionalCategoryDropdown(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    categories: List<String>,
    label: String = "Category",
    modifier: Modifier = Modifier
) {
    val displayValue = selectedCategory ?: "All"
    val allCategories = listOf("All") + categories

    CategoryDropdown(
        selectedCategory = displayValue,
        onCategorySelected = { selected ->
            onCategorySelected(if (selected == "All") null else selected)
        },
        categories = allCategories,
        label = label,
        modifier = modifier
    )
}

@Composable
fun DatePickerField(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    label: String = "Date",
    modifier: Modifier = Modifier,
    maxDate: Long? = null
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    OutlinedTextField(
        value = dateFormat.format(Date(selectedDate)),
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        cal.set(year, month, dayOfMonth)
                        onDateSelected(cal.timeInMillis)
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    maxDate?.let { datePicker.maxDate = it }
                }.show()
            }
    )
}

@Composable
fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Amount",
    maxLength: Int = 7,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = { 
            if (it.length <= maxLength && (it.isEmpty() || it.all { char -> char.isDigit() })) {
                onValueChange(it)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction
        ),
        isError = isError,
        supportingText = errorMessage?.let { { Text(it) } },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun NoteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Note (Optional)",
    maxLength: Int = 30,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= maxLength) onValueChange(it) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun FormSectionSpacing(height: androidx.compose.ui.unit.Dp = 16.dp) {
    Spacer(modifier = Modifier.height(height))
}
