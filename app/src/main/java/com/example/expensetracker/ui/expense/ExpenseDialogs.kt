package com.example.expensetracker.ui.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.expensetracker.data.Expense
import com.example.expensetracker.ui.categories

// Dialog for adding or editing an expense
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditorDialog(
    editingExpense: Expense?,
    budgetId: Int,
    isPieChartVisible: MutableState<Boolean>,
    isBarChartVisible: MutableState<Boolean>,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, Long) -> Unit
) {
    // Stores what the user types
    var dialogTitle by remember { mutableStateOf(editingExpense?.title ?: "") }
    var dialogAmount by remember { mutableStateOf(editingExpense?.amount?.toString() ?: "") }
    var dialogCategory by remember { mutableStateOf(editingExpense?.category ?: "Food") }
    val dialogTimestamp = remember { mutableStateOf(editingExpense?.timestamp ?: System.currentTimeMillis()) }
    var errorMessage by remember { mutableStateOf("") }
    val showAdvanced = remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dialogTimestamp.value)

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 120.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Title changes based on if we are adding or editing
                Text(
                    if (editingExpense == null) "Add Expense" else "Edit Expense",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    // Title input
                    OutlinedTextField(
                        value = dialogTitle,
                        onValueChange = { dialogTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Amount input
                    OutlinedTextField(
                        value = dialogAmount,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) dialogAmount = it },
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Category dropdown
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = dialogCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = { dialogCategory = category; expanded = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Date", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Date picker
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        title = null,
                        headline = null,
                        colors = DatePickerDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Advanced options
                    if (showAdvanced.value) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Show Pie Chart", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = isPieChartVisible.value,
                                onCheckedChange = { isPieChartVisible.value = it }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Show Bar Chart", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = isBarChartVisible.value,
                                onCheckedChange = { isBarChartVisible.value = it }
                            )
                        }
                    }
                    // Show error if something is wrong
                    if (errorMessage.isNotBlank()) {
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { showAdvanced.value = !showAdvanced.value }) {
                        Text(if (showAdvanced.value) "Hide Advanced" else "Show Advanced")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onDismiss() }) { Text("Cancel") }
                    TextButton(onClick = {
                        val amt = dialogAmount.toDoubleOrNull() ?: 0.0
                        val decimalPart = dialogAmount.substringAfter(".", "")
                        val selectedDate = datePickerState.selectedDateMillis ?: dialogTimestamp.value
                        if (dialogTitle.isBlank()) errorMessage = "Please enter a title"
                        else if (amt <= 0) errorMessage = "Please enter a valid amount"
                        else if (dialogAmount.contains(".") && decimalPart.length > 2) errorMessage = "Max 2 decimal places"
                        else {
                            onSave(dialogTitle, amt, dialogCategory, selectedDate)
                            onDismiss()
                        }
                    }) { Text("Save") }
                }
            }
        }
    }
}

// Dialog to confirm deleting an expense
@Composable
fun DeleteExpenseDialog(expenseTitle: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Delete Expense") },
        text = { Text("Are you sure you want to delete $expenseTitle?") },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancel") }
        }
    )
}