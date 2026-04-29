@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.expensetracker.data.Budget
import com.example.expensetracker.data.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// List of expense categories for the dropdown
val categories = listOf(
    "Housing", "Food", "Transportation", "Gas", "Bills", "Shopping",
    "Entertainment", "Health", "Education", "Travel", "Savings", "Other"
)

// Colors for each category used in the pie chart
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

// The different time range options the user can pick
enum class TimeRange(val label: String) {
    ONE_WEEK("1 Week"),
    TWO_WEEKS("2 Weeks"),
    MONTHLY("Monthly")
}

// Draws a pie chart showing spending by category
@Composable
fun CategoryPieChart(expenses: List<Expense>, modifier: Modifier = Modifier) {
    if (expenses.isEmpty()) return

    // Add up all spending
    val totalSpent = expenses.sumOf { it.amount }

    // Group expenses by category and add up each one
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            var startAngle = -90f
            // Draw each category as a slice of the pie
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

// Shows a bar chart of spending per category
@Composable
fun CategoryBarChart(expenses: List<Expense>, modifier: Modifier = Modifier) {
    if (expenses.isEmpty()) return

    // Group expenses by category and add up each one
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    // Find the biggest amount so we can scale the bars
    val maxAmount = categoryTotals.values.maxOrNull() ?: 1.0

    Column(modifier = modifier.fillMaxWidth()) {
        Text("Spending by Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Draw a bar for each category
        categoryTotals.forEach { (category, amount) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category name on the left
                Text(
                    text = category,
                    modifier = Modifier.width(100.dp),
                    style = MaterialTheme.typography.labelSmall
                )
                // The bar itself
                LinearProgressIndicator(
                    progress = { (amount / maxAmount).toFloat() },
                    modifier = Modifier.weight(1f).height(12.dp),
                    color = categoryColors[category] ?: Color.Gray,
                    strokeCap = StrokeCap.Round
                )
                // Amount on the right
                Text(
                    text = "$${"%.0f".format(amount)}",
                    modifier = Modifier.width(50.dp).padding(start = 8.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// Main screen that shows either the budget list or a budget's details
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
        AlertDialog(
            onDismissRequest = { budgetToDelete = null },
            title = { Text("Delete Budget") },
            text = { Text("Are you sure you want to delete '${budgetToDelete?.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteBudget(budgetToDelete!!)
                    budgetToDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { budgetToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

// A single budget card shown in the budget list
@Composable
fun BudgetCard(
    budget: Budget,
    totalSpent: Double,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Controls the dropdown menu
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onSelect() }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Budget icon
            Text(budget.icon, modifier = Modifier.padding(end = 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Budget name and spending info
                Text(budget.name, style = MaterialTheme.typography.titleLarge)
                Text("$${"%.2f".format(totalSpent)} of $${"%.2f".format(budget.amount)} spent")

                // Show days left if end date is set
                if (budget.endDate > 0L) {
                    val daysLeft = ((budget.endDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                    val statusText = when {
                        daysLeft < 0 -> "Cycle finished"
                        daysLeft == 0 -> "Ends today"
                        else -> "$daysLeft days left"
                    }
                    Text(statusText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar showing how much of the budget is used
                val progress = if (budget.amount > 0) (totalSpent / budget.amount).toFloat().coerceAtMost(1f) else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    strokeCap = StrokeCap.Round
                )
            }
            // Three dot menu for edit and delete
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Budget Options")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            expanded = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            expanded = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }
}

// Dialog for editing an existing budget
@Composable
fun EditBudgetDialog(budget: Budget, onDismiss: () -> Unit, onConfirm: (Budget) -> Unit) {
    var name by remember { mutableStateOf(budget.name) }
    var amount by remember { mutableStateOf(budget.amount.toString()) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = if (budget.endDate > 0) budget.endDate else null)

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
                Text("Edit Budget", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    // Budget name input
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Budget Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    // Budget amount input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                        label = { Text("Limit Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Set End Date", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Date picker for end date
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        title = null,
                        headline = null,
                        colors = DatePickerDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onDismiss() }) { Text("Cancel") }
                    TextButton(onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank() && amt > 0) {
                            onConfirm(budget.copy(name = name, amount = amt, endDate = datePickerState.selectedDateMillis ?: 0L))
                        }
                    }) { Text("Save") }
                }
            }
        }
    }
}

// Dialog for adding a new budget
@Composable
fun AddBudgetDialog(onDismiss: () -> Unit, onConfirm: (String, Double, Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()

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
                Text("New Budget", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    // Budget name input
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Budget Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    // Budget amount input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                        label = { Text("Limit Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Set End Date", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Date picker for end date
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        title = null,
                        headline = null,
                        colors = DatePickerDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onDismiss() }) { Text("Cancel") }
                    TextButton(onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank() && amt > 0) {
                            onConfirm(name, amt, datePickerState.selectedDateMillis ?: 0L)
                        }
                    }) { Text("Create") }
                }
            }
        }
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

    // State for add/edit expense dialog
    var dialogTitle by remember { mutableStateOf("") }
    var dialogAmount by remember { mutableStateOf("") }
    var dialogCategory by remember { mutableStateOf("Food") }
    var dialogTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val isPieChartVisible = remember { mutableStateOf(true) }
    val isBarChartVisible = remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val editingExpense: MutableState<Expense?> = remember { mutableStateOf(null) }
    val deleteTarget: MutableState<Expense?> = remember { mutableStateOf(null) }
    val showEditorDialog = remember { mutableStateOf(false) }
    val showAdvanced = remember { mutableStateOf(false) }

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
                    // Show a chip for each time range option
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
                editingExpense.value = null
                dialogTitle = ""
                dialogAmount = ""
                dialogCategory = "Food"
                dialogTimestamp = System.currentTimeMillis()
                errorMessage = ""
                showAdvanced.value = false
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
                            editingExpense.value = expense
                            dialogTitle = expense.title
                            dialogAmount = expense.amount.toString()
                            dialogCategory = expense.category
                            dialogTimestamp = expense.timestamp
                            errorMessage = ""
                            showAdvanced.value = false
                            showEditorDialog.value = true
                        },
                        onDelete = { deleteTarget.value = expense }
                    )
                }
            }
        }
    }

    // Add/Edit expense dialog
    if (showEditorDialog.value) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dialogTimestamp)
        Dialog(
            onDismissRequest = { showEditorDialog.value = false },
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
                    Text(
                        if (editingExpense.value == null) "Add Expense" else "Edit Expense",
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
                        // Advanced options toggle
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
                        TextButton(onClick = { showEditorDialog.value = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            val amt = dialogAmount.toDoubleOrNull() ?: 0.0
                            val decimalPart = dialogAmount.substringAfter(".", "")
                            val selectedDate = datePickerState.selectedDateMillis ?: dialogTimestamp
                            if (dialogTitle.isBlank()) errorMessage = "Please enter a title"
                            else if (amt <= 0) errorMessage = "Please enter a valid amount"
                            else if (dialogAmount.contains(".") && decimalPart.length > 2) errorMessage = "Max 2 decimal places"
                            else {
                                if (editingExpense.value == null) onAddExpense(dialogTitle, amt, dialogCategory, budget.id, selectedDate)
                                else onUpdateExpense(editingExpense.value!!.copy(title = dialogTitle, amount = amt, category = dialogCategory, timestamp = selectedDate))
                                showEditorDialog.value = false
                            }
                        }) { Text("Save") }
                    }
                }
            }
        }
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

    // Delete expense confirmation dialog
    if (deleteTarget.value != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget.value = null },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete ${deleteTarget.value?.title}?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteExpense(deleteTarget.value!!)
                    deleteTarget.value = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget.value = null }) { Text("Cancel") }
            }
        )
    }
}