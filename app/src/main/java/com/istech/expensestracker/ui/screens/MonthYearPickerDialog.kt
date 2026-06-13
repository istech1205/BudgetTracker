package com.istech.expensestracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun MonthYearPickerDialog(
    initialMonth: Int,
    initialYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (month: Int, year: Int) -> Unit
) {
    var selectedMonth by remember { mutableIntStateOf(initialMonth) }
    var selectedYear by remember { mutableIntStateOf(initialYear) }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Month") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Month selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            selectedMonth = if (selectedMonth > 1) selectedMonth - 1 else 12
                        }
                    ) {
                        Text("<")
                    }

                    Text(
                        text = months[selectedMonth - 1],
                        style = MaterialTheme.typography.titleMedium
                    )

                    TextButton(
                        onClick = {
                            selectedMonth = if (selectedMonth < 12) selectedMonth + 1 else 1
                        }
                    ) {
                        Text(">")
                    }
                }

                // Year selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            if (selectedYear > currentYear - 10) {
                                selectedYear--
                            }
                        }
                    ) {
                        Text("<")
                    }

                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )

                    TextButton(
                        onClick = {
                            if (selectedYear < currentYear + 10) {
                                selectedYear++
                            }
                        }
                    ) {
                        Text(">")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedMonth, selectedYear) }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
