@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Expense

// List of categories for the dropdown
val categories = listOf(
    "Housing",
    "Food",
    "Transportation",
    "Gas",
    "Bills",
    "Shopping",
    "Entertainment",
    "Health",
    "Education",
    "Travel",
    "Savings",
    "Other"
)

// Assign a color to each category for the pie chart
val categoryColors = mapOf(
    "Housing" to Color(0xFFE57373),
    "Food" to Color(0xFF81C784),
    "Transportation" to Color(0xFF64B5F6),
    "Gas" to Color(0xFFFFD54F),
    "Bills" to Color(0xFFBA68C8),
    "Shopping" to Color(0xFFF06292),
    "Entertainment" to Color(0xFF4DB6AC),
    "Health" to Color(0xFFFF8A65),
    "Education" to Color(0xFF9575CD),
    "Travel" to Color(0xFFAED581),
    "Savings" to Color(0xFFFFB74D),
    "Other" to Color(0xFF90A4AE)
)

@Composable
fun CategoryPieChart(expenses: List<Expense>, modifier: Modifier = Modifier) {
    if (expenses.isEmpty()) return

    val totalSpent = expenses.sumOf { it.amount }
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(150.dp)) {
            var startAngle = -90f
            categoryTotals.forEach { (category, amount) ->
                val sweepAngle = (amount / totalSpent).toFloat() * 360f
                drawArc(
                    color = categoryColors[category] ?: Color.Gray,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }
    }
}

@Composable
fun HomeScreen(
    expenses: List<Expense>,
    onAddExpense: (String, Double, String, Double) -> Unit,
    onUpdateExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {

    // Adds up all expense amounts for the total card
    val totalSpent = expenses.sumOf { it.amount }
    
    // Adds up all budgets set for the expenses
    val totalBudget = expenses.sumOf { it.budget }

    // Title typed into the popup
    var dialogTitle by remember { mutableStateOf("") }

    // Amount typed into the popup
    var dialogAmount by remember { mutableStateOf("") }

    // Category selected in the popup
    var dialogCategory by remember { mutableStateOf("Food") }

    // Budget state for the popup
    var isBudgetEnabled by remember { mutableStateOf(false) }
    var dialogBudgetAmount by remember { mutableStateOf("") }

    // Pie Chart Toggle state
    var isPieChartVisible by remember { mutableStateOf(true) }

    // Error message shown if the input is bad
    var errorMessage by remember { mutableStateOf("") }

    // If this is null, we are adding a new expense
    // If this has a value, we are editing an old one
    var editingExpense by remember { mutableStateOf<Expense?>(null) }

    // This stores the expense the user wants to delete
    var deleteTarget by remember { mutableStateOf<Expense?>(null) }

    // Controls whether the add/edit popup is visible
    var showEditorDialog by remember { mutableStateOf(false) }

    // Controls whether the advanced options are visible
    var showAdvanced by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top card showing the total amount and budget
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Overview",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Column {
                            Text(
                                "Spent",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "$${"%.2f".format(totalSpent)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (totalBudget > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column {
                                Text(
                                    "Budget",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    "$${"%.2f".format(totalBudget)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = if (totalSpent > totalBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // --- PIE CHART INTEGRATION ---
                    if (expenses.isNotEmpty() && isPieChartVisible) {
                        CategoryPieChart(
                            expenses = expenses,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                
                if (totalBudget > 0) {
                    val spentPercentage = (totalSpent / totalBudget).toFloat()
                    
                    // Logic for progress bar color
                    val barColor = when {
                        spentPercentage < 0.7f -> Color(0xFF81C784) // Normal: Green
                        spentPercentage <= 1.0f -> Color(0xFFFFB74D) // Warning: Orange
                        else -> Color(0xFFE57373) // Over Budget: Red
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- BUDGET PROGRESS BAR ---
                    LinearProgressIndicator(
                        progress = { spentPercentage.coerceAtMost(1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = barColor,
                        trackColor = barColor.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )

                    val remaining = totalBudget - totalSpent
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (remaining >= 0) "Remaining: $${"%.2f".format(remaining)}" else "Over Budget: $${"%.2f".format(-remaining)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = barColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add button opens the popup with empty fields
        Button(
            onClick = {
                editingExpense = null
                dialogTitle = ""
                dialogAmount = ""
                dialogCategory = "Food"
                isBudgetEnabled = false
                dialogBudgetAmount = ""
                errorMessage = ""
                showAdvanced = false
                showEditorDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Add Expense")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Recent Expenses",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (expenses.isEmpty()) {
            Text("No expenses yet")
        } else {
            LazyColumn {
                items(expenses) { expense ->
                    ExpenseRow(
                        expense = expense,

                        // Edit button loads old values into the popup
                        onEdit = {
                            editingExpense = expense
                            dialogTitle = expense.title
                            dialogAmount = expense.amount.toString()
                            dialogCategory = expense.category
                            isBudgetEnabled = expense.budget > 0
                            dialogBudgetAmount = if (expense.budget > 0) expense.budget.toString() else ""
                            errorMessage = ""
                            showAdvanced = false
                            showEditorDialog = true
                        },

                        // Delete button opens the confirmation popup
                        onDelete = {
                            deleteTarget = expense
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showEditorDialog) {
            AlertDialog(
                onDismissRequest = {
                    showEditorDialog = false
                    errorMessage = ""
                },

                title = {
                    Text(
                        if (editingExpense == null) "Add Expense"
                        else "Edit Expense"
                    )
                },

                text = {
                    Column {
                        // Expense title input
                        OutlinedTextField(
                            value = dialogTitle,
                            onValueChange = { dialogTitle = it },
                            label = { Text("Expense Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Amount input
                        OutlinedTextField(
                            value = dialogAmount,
                            onValueChange = { 
                                // Only allow numbers and one decimal point
                                if (it.all { char -> char.isDigit() || char == '.' } && it.count { char -> char == '.' } <= 1) {
                                    dialogAmount = it
                                }
                            },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category Dropdown
                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = dialogCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(text = category) },
                                        onClick = {
                                            dialogCategory = category
                                            expanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Advanced Options toggle
                        TextButton(
                            onClick = { showAdvanced = !showAdvanced }
                        ) {
                            Text(if (showAdvanced) "Hide Advanced Options" else "Show Advanced Options")
                        }

                        // Advanced options section
                        AnimatedVisibility(visible = showAdvanced) {
                            Column {
                                // Pie Chart Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Show Pie Chart", style = MaterialTheme.typography.bodyLarge)
                                    Switch(
                                        checked = isPieChartVisible,
                                        onCheckedChange = { isPieChartVisible = it }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Set Budget Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Set Budget", style = MaterialTheme.typography.bodyLarge)
                                    Switch(
                                        checked = isBudgetEnabled,
                                        onCheckedChange = { isBudgetEnabled = it }
                                    )
                                }

                                if (isBudgetEnabled) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = dialogBudgetAmount,
                                        onValueChange = { 
                                            // Only allow numbers and one decimal point
                                            if (it.all { char -> char.isDigit() || char == '.' } && it.count { char -> char == '.' } <= 1) {
                                                dialogBudgetAmount = it
                                            }
                                        },
                                        label = { Text("Budget Amount") },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // Show validation error in red
                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            val amount = dialogAmount.toDoubleOrNull()
                            val budget = if (isBudgetEnabled) dialogBudgetAmount.toDoubleOrNull() ?: 0.0 else 0.0
                            val decimalPart = dialogAmount.substringAfter(".", "")
                            val hasTooManyDecimals = decimalPart.contains(".") && decimalPart.length > 2
                            
                            val budgetDecimalPart = dialogBudgetAmount.substringAfter(".", "")
                            val budgetHasTooManyDecimals = isBudgetEnabled && dialogBudgetAmount.contains(".") && budgetDecimalPart.length > 2

                            // Basic validation
                            if (dialogTitle.isBlank()) {
                                errorMessage = "Please enter a title"
                            } else if (amount == null) {
                                errorMessage = "Please enter a valid number"
                            } else if (hasTooManyDecimals) {
                                errorMessage = "Amount: Use no more than 2 decimal places"
                            } else if (amount <= 0) {
                                errorMessage = "Amount must be greater than 0"
                            } else if (dialogCategory.isBlank()) {
                                errorMessage = "Please enter a category"
                            } else if (isBudgetEnabled && dialogBudgetAmount.isNotBlank() && dialogBudgetAmount.toDoubleOrNull() == null) {
                                errorMessage = "Please enter a valid budget"
                            } else if (budgetHasTooManyDecimals) {
                                errorMessage = "Budget: Use no more than 2 decimal places"
                            } else {

                                if (editingExpense == null) {
                                    // Add a brand new expense
                                    onAddExpense(dialogTitle, amount, dialogCategory, budget)
                                } else {
                                    // Update the existing expense
                                    onUpdateExpense(
                                        editingExpense!!.copy(
                                            title = dialogTitle,
                                            amount = amount,
                                            category = dialogCategory,
                                            budget = budget
                                        )
                                    )
                                }

                                // Reset popup after success
                                dialogTitle = ""
                                dialogAmount = ""
                                dialogCategory = "Food"
                                isBudgetEnabled = false
                                dialogBudgetAmount = ""
                                errorMessage = ""
                                editingExpense = null
                                showEditorDialog = false
                            }
                        }
                    ) {
                        Text(
                            if (editingExpense == null) "Add"
                            else "Save"
                        )
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = {
                            showEditorDialog = false
                            errorMessage = ""
                            editingExpense = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete confirmation popup
        deleteTarget?.let { expense ->
            AlertDialog(
                onDismissRequest = {
                    deleteTarget = null
                },

                title = {
                    Text("Delete Expense")
                },

                text = {
                    Text("Are you sure you want to delete ${expense.title}?")
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteExpense(expense)
                            deleteTarget = null
                        }
                    ) {
                        Text("Delete")
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = {
                            deleteTarget = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}