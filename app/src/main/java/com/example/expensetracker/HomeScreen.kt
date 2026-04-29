@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.expensetracker.data.Budget
import com.example.expensetracker.data.Expense
import com.example.expensetracker.ui.TimeRange
import com.example.expensetracker.ui.budget.AddBudgetDialog
import com.example.expensetracker.ui.budget.BudgetCard
import com.example.expensetracker.ui.budget.DeleteBudgetDialog
import com.example.expensetracker.ui.budget.EditBudgetDialog
import com.example.expensetracker.ui.charts.CategoryBarChart
import com.example.expensetracker.ui.charts.CategoryPieChart
import com.example.expensetracker.ui.expense.DeleteExpenseDialog
import com.example.expensetracker.ui.expense.ExpenseEditorDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Main screen that shows either the budget list or a budget details
@Composable
fun HomeScreen(
    expenses: List<Expense>,
    budgets: List<Budget>,
    selectedBudget: Budget?,
    onBudgetSelected: (Budget?) -> Unit,
    onAddBudget: (String, Double, Long) -> Unit,
    onUpdateBudget: (Budget) -> Unit,
    onDeleteBudget: (Budget) -> Unit,
    onAddExpense: (String, Double, String, Int, Long) -> Unit,
    onUpdateExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    // Controls for showing dialogs
    val showAddBudgetDialog = remember { mutableStateOf(false) }
    var budgetToEdit by remember { mutableStateOf<Budget?>(null) }
    var budgetToDelete by remember { mutableStateOf<Budget?>(null) }

    if (selectedBudget == null) {
        // Show the list of all budgets
        Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Budget Overview", style = MaterialTheme.typography.titleLarge)
                // Button to add a new budget
                IconButton(onClick = { showAddBudgetDialog.value = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Budget")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (budgets.isEmpty()) {
                Text("No budgets yet. Click + to add one!")
            } else {
                // Show each budget as a card
                LazyColumn {
                    items(budgets) { budget ->
                        val budgetExpenses = expenses.filter { it.budgetId == budget.id }
                        val totalSpent = budgetExpenses.sumOf { it.amount }
                        BudgetCard(
                            budget = budget,
                            totalSpent = totalSpent,
                            onSelect = { onBudgetSelected(budget) },
                            onEdit = { budgetToEdit = budget },
                            onDelete = { budgetToDelete = budget }
                        )
                    }
                }
            }
        }
    } else {
        // Show the details of the selected budget
        BudgetDetailView(
            budget = selectedBudget,
            expenses = expenses.filter { it.budgetId == selectedBudget.id },
            onAddExpense = onAddExpense,
            onUpdateExpense = onUpdateExpense,
            onDeleteExpense = onDeleteExpense,
            onUpdateBudget = onUpdateBudget,
            modifier = modifier
        )
    }

    // Show add budget dialog if needed
    if (showAddBudgetDialog.value) {
        AddBudgetDialog(
            onDismiss = { showAddBudgetDialog.value = false },
            onConfirm = { name, amount, endDate ->
                onAddBudget(name, amount, endDate)
                showAddBudgetDialog.value = false
            }
        )
    }

    // Show edit budget dialog if needed
    if (budgetToEdit != null) {
        EditBudgetDialog(
            budget = budgetToEdit!!,
            onDismiss = { budgetToEdit = null },
            onConfirm = { updatedBudget ->
                onUpdateBudget(updatedBudget)
                budgetToEdit = null
            }
        )
    }

    // Show delete budget confirmation if needed
    if (budgetToDelete != null) {
        DeleteBudgetDialog(
            budgetName = budgetToDelete!!.name,
            onDismiss = { budgetToDelete = null },
            onConfirm = {
                onDeleteBudget(budgetToDelete!!)
                budgetToDelete = null
            }
        )
    }
}

// Shows the details of a single budget including expenses and charts
@Composable
fun BudgetDetailView(
    budget: Budget,
    expenses: List<Expense>,
    onAddExpense: (String, Double, String, Int, Long) -> Unit,
    onUpdateExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onUpdateBudget: (Budget) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current month and year for filtering
    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    // Currently selected time range (default is monthly)
    var selectedTimeRange by remember { mutableStateOf(TimeRange.MONTHLY) }

    val showEditDateDialog = remember { mutableStateOf(false) }
    val showEditAmountDialog = remember { mutableStateOf(false) }
    val showEditorDialog = remember { mutableStateOf(false) }
    val isPieChartVisible = remember { mutableStateOf(true) }
    val isBarChartVisible = remember { mutableStateOf(true) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var deleteTarget by remember { mutableStateOf<Expense?>(null) }

    // Filter expenses based on the selected time range
    val filteredExpenses = expenses.filter {
        val expenseTime = it.timestamp
        val now = System.currentTimeMillis()
        when (selectedTimeRange) {
            // Show expenses from the last 7 days
            TimeRange.ONE_WEEK -> {
                val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000L)
                expenseTime >= oneWeekAgo && expenseTime <= now
            }
            // Show expenses from the last 14 days
            TimeRange.TWO_WEEKS -> {
                val twoWeeksAgo = now - (14 * 24 * 60 * 60 * 1000L)
                expenseTime >= twoWeeksAgo && expenseTime <= now
            }
            // Show expenses for the selected month and year
            TimeRange.MONTHLY -> {
                val cal = Calendar.getInstance().apply { timeInMillis = expenseTime }
                cal.get(Calendar.MONTH) == selectedMonth && cal.get(Calendar.YEAR) == selectedYear
            }
        }
    }

    val totalSpent = filteredExpenses.sumOf { it.amount }
    val totalBudget = budget.amount
    val today = Calendar.getInstance()

    // Figure out the cycle status text
    val cycleStatus = if (budget.endDate > 0L) {
        val daysLeft = ((budget.endDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
        when {
            daysLeft < 0 -> "Cycle finished"
            daysLeft == 0 -> "Cycle ends today"
            else -> "Ends in $daysLeft days"
        }
    } else {
        if (selectedMonth == today.get(Calendar.MONTH) && selectedYear == today.get(Calendar.YEAR)) {
            val daysLeft = today.getActualMaximum(Calendar.DAY_OF_MONTH) - today.get(Calendar.DAY_OF_MONTH)
            if (daysLeft == 0) "Cycle ends today" else "Ends in $daysLeft days"
        } else if (selectedYear < today.get(Calendar.YEAR) || (selectedYear == today.get(Calendar.YEAR) && selectedMonth < today.get(Calendar.MONTH))) {
            "Cycle finished"
        } else "Cycle starts later"
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Top summary card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.clickable { showEditDateDialog.value = true }) {
                        val headerDate = if (budget.endDate > 0L) Date(budget.endDate) else {
                            Calendar.getInstance().apply {
                                set(Calendar.MONTH, selectedMonth)
                                set(Calendar.YEAR, selectedYear)
                            }.time
                        }
                        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(headerDate)
                        Text(monthName, style = MaterialTheme.typography.titleMedium)
                        Text(cycleStatus, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time range filter chips next to each other
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeRange.values().forEach { range ->
                        FilterChip(
                            selected = selectedTimeRange == range,
                            onClick = { selectedTimeRange = range },
                            label = { Text(range.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Spending summary and pie chart side by side
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Spent", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "$${"%.2f".format(totalSpent)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Tap to edit the budget amount
                        Column(modifier = Modifier.clickable { showEditAmountDialog.value = true }) {
                            Text("Budget", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "$${"%.2f".format(totalBudget)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // Show pie chart if there are expenses
                    if (filteredExpenses.isNotEmpty() && isPieChartVisible.value) {
                        CategoryPieChart(expenses = filteredExpenses, modifier = Modifier.padding(start = 16.dp))
                    }
                }

                // Progress bar showing budget usage
                val spentPercentage = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
                val barColor = when {
                    spentPercentage < 0.7f -> Color(0xFF81C784)
                    spentPercentage <= 1.0f -> Color(0xFFFFB74D)
                    else -> Color(0xFFE57373)
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { spentPercentage.coerceAtMost(1f) },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = barColor,
                    strokeCap = StrokeCap.Round
                )

                // Show bar chart if there are expenses
                if (filteredExpenses.isNotEmpty() && isBarChartVisible.value) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CategoryBarChart(expenses = filteredExpenses)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to add a new expense
        Button(
            onClick = {
                editingExpense = null
                showEditorDialog.value = true
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("+ Add Expense") }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Expenses", style = MaterialTheme.typography.titleMedium)

        // Show expenses or empty message
        if (filteredExpenses.isEmpty()) {
            Text("No expenses found.")
        } else {
            LazyColumn {
                items(filteredExpenses) { expense ->
                    ExpenseRow(
                        expense = expense,
                        onEdit = {
                            editingExpense = expense
                            showEditorDialog.value = true
                        },
                        onDelete = { deleteTarget = expense }
                    )
                }
            }
        }
    }

    // Add/Edit expense dialog
    if (showEditorDialog.value) {
        ExpenseEditorDialog(
            editingExpense = editingExpense,
            budgetId = budget.id,
            isPieChartVisible = isPieChartVisible,
            isBarChartVisible = isBarChartVisible,
            onDismiss = { showEditorDialog.value = false },
            onSave = { title, amount, category, timestamp ->
                if (editingExpense == null) {
                    onAddExpense(title, amount, category, budget.id, timestamp)
                } else {
                    onUpdateExpense(editingExpense!!.copy(title = title, amount = amount, category = category, timestamp = timestamp))
                }
            }
        )
    }

    // Delete expense confirmation dialog
    if (deleteTarget != null) {
        DeleteExpenseDialog(
            expenseTitle = deleteTarget!!.title,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                onDeleteExpense(deleteTarget!!)
                deleteTarget = null
            }
        )
    }

    // Dialog to edit the budget end date
    if (showEditDateDialog.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (budget.endDate > 0L) budget.endDate else Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis
        )
        Dialog(
            onDismissRequest = { showEditDateDialog.value = false },
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
                    Text("Set Budget End Date", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        Text("Choose when this budget ends to track days left.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        DatePicker(
                            state = datePickerState,
                            showModeToggle = false,
                            title = null,
                            headline = null,
                            colors = DatePickerDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditDateDialog.value = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            val dateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                            onUpdateBudget(budget.copy(endDate = dateMillis))
                            val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                            selectedMonth = cal.get(Calendar.MONTH)
                            selectedYear = cal.get(Calendar.YEAR)
                            showEditDateDialog.value = false
                        }) { Text("Apply") }
                    }
                }
            }
        }
    }

    // Dialog to edit the budget spending limit
    if (showEditAmountDialog.value) {
        var amountText by remember { mutableStateOf(budget.amount.toString()) }
        AlertDialog(
            onDismissRequest = { showEditAmountDialog.value = false },
            title = { Text("Edit Budget Amount") },
            text = {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amountText = it },
                    label = { Text("Limit Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onUpdateBudget(budget.copy(amount = amt))
                        showEditAmountDialog.value = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditAmountDialog.value = false }) { Text("Cancel") }
            }
        )
    }
}