@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.example.expensetracker.data.Budget
import com.example.expensetracker.data.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// List of categories for the dropdown
val categories = listOf(
    "Housing", "Food", "Transportation", "Gas", "Bills", "Shopping",
    "Entertainment", "Health", "Education", "Travel", "Savings", "Other"
)

// Category colors for the pie chart
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
        Canvas(modifier = Modifier.size(120.dp)) {
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
    budgets: List<Budget>,
    selectedBudget: Budget?,
    onBudgetSelected: (Budget?) -> Unit,
    onAddBudget: (String, Double) -> Unit,
    onAddExpense: (String, Double, String, Int, Long) -> Unit,
    onUpdateExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    val showAddBudgetDialog = remember { mutableStateOf(false) }

    if (selectedBudget == null) {
        Scaffold(
            modifier = modifier,
            topBar = { TopAppBar(title = { Text("Your Budgets") }) }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Budget Overview", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { showAddBudgetDialog.value = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Budget")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (budgets.isEmpty()) {
                    Text("No budgets yet. Click + to add one!")
                } else {
                    LazyColumn {
                        items(budgets) { budget ->
                            val budgetExpenses = expenses.filter { it.budgetId == budget.id }
                            val totalSpent = budgetExpenses.sumOf { it.amount }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { onBudgetSelected(budget) }
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(budget.icon, modifier = Modifier.padding(end = 16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(budget.name, style = MaterialTheme.typography.titleLarge)
                                        Text("$${"%.2f".format(totalSpent)} of $${"%.2f".format(budget.amount)} spent")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val progress = if (budget.amount > 0) (totalSpent / budget.amount).toFloat().coerceAtMost(1f) else 0f
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier.fillMaxWidth(),
                                            strokeCap = StrokeCap.Round
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        BudgetDetailView(
            budget = selectedBudget,
            expenses = expenses.filter { it.budgetId == selectedBudget.id },
            onBack = { onBudgetSelected(null) },
            onAddExpense = onAddExpense,
            onUpdateExpense = onUpdateExpense,
            onDeleteExpense = onDeleteExpense,
            modifier = modifier
        )
    }

    if (showAddBudgetDialog.value) {
        AddBudgetDialog(
            onDismiss = { showAddBudgetDialog.value = false },
            onConfirm = { name, amount ->
                onAddBudget(name, amount)
                showAddBudgetDialog.value = false
            }
        )
    }
}

@Composable
fun AddBudgetDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("New Budget") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Budget Name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                    label = { Text("Limit Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && amt > 0) onConfirm(name, amt)
            }) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailView(
    budget: Budget,
    expenses: List<Expense>,
    onBack: () -> Unit,
    onAddExpense: (String, Double, String, Int, Long) -> Unit,
    onUpdateExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    val showFilterDialog = remember { mutableStateOf(false) }

    val filteredExpenses = expenses.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
        cal.get(Calendar.MONTH) == selectedMonth && cal.get(Calendar.YEAR) == selectedYear
    }

    val totalSpent = filteredExpenses.sumOf { it.amount }
    val totalBudget = budget.amount

    val today = Calendar.getInstance()
    val cycleStatus = if (selectedMonth == today.get(Calendar.MONTH) && selectedYear == today.get(Calendar.YEAR)) {
        val daysLeft = today.getActualMaximum(Calendar.DAY_OF_MONTH) - today.get(Calendar.DAY_OF_MONTH)
        if (daysLeft == 0) "Cycle ends today" else "Ends in $daysLeft days"
    } else if (selectedYear < today.get(Calendar.YEAR) || (selectedYear == today.get(Calendar.YEAR) && selectedMonth < today.get(Calendar.MONTH))) {
        "Cycle finished"
    } else "Cycle starts later"

    var dialogTitle by remember { mutableStateOf("") }
    var dialogAmount by remember { mutableStateOf("") }
    var dialogCategory by remember { mutableStateOf("Food") }
    var dialogTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val showDatePicker = remember { mutableStateOf(false) }
    val isPieChartVisible = remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val editingExpense: MutableState<Expense?> = remember { mutableStateOf(null) }
    val deleteTarget: MutableState<Expense?> = remember { mutableStateOf(null) }
    val showEditorDialog = remember { mutableStateOf(false) }
    val showAdvanced = remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier, 
        topBar = {
            TopAppBar(
                title = { Text(budget.name) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            val calendar = Calendar.getInstance().apply { set(Calendar.MONTH, selectedMonth); set(Calendar.YEAR, selectedYear) }
                            val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
                            Text(monthName, style = MaterialTheme.typography.titleMedium)
                            Text(cycleStatus, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                        IconButton(onClick = { showFilterDialog.value = true }) { Icon(Icons.AutoMirrored.Filled.List, "Filter") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Spent", style = MaterialTheme.typography.labelMedium)
                            Text("$${"%.2f".format(totalSpent)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Budget", style = MaterialTheme.typography.labelMedium)
                            Text("$${"%.2f".format(totalBudget)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                        if (filteredExpenses.isNotEmpty() && isPieChartVisible.value) {
                            CategoryPieChart(expenses = filteredExpenses, modifier = Modifier.padding(start = 16.dp))
                        }
                    }
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
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { 
                editingExpense.value = null; dialogTitle = ""; dialogAmount = ""; dialogCategory = "Food"
                dialogTimestamp = System.currentTimeMillis(); errorMessage = ""; showAdvanced.value = false; showEditorDialog.value = true 
            }, modifier = Modifier.fillMaxWidth()) { Text("+ Add Expense") }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Expenses", style = MaterialTheme.typography.titleMedium)
            if (filteredExpenses.isEmpty()) {
                Text("No expenses found.")
            } else {
                LazyColumn {
                    items(filteredExpenses) { expense ->
                        ExpenseRow(expense = expense, onEdit = {
                            editingExpense.value = expense; dialogTitle = expense.title; dialogAmount = expense.amount.toString()
                            dialogCategory = expense.category; dialogTimestamp = expense.timestamp; errorMessage = ""
                            showAdvanced.value = false; showEditorDialog.value = true
                        }, onDelete = { deleteTarget.value = expense })
                    }
                }
            }
        }
    }

    if (showEditorDialog.value) {
        AlertDialog(
            onDismissRequest = { showEditorDialog.value = false },
            title = { Text(if (editingExpense.value == null) "Add Expense" else "Edit Expense") },
            text = {
                Column {
                    OutlinedTextField(value = dialogTitle, onValueChange = { dialogTitle = it }, label = { Text("Title") })
                    OutlinedTextField(value = dialogAmount, onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) dialogAmount = it }, label = { Text("Amount") })
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = dialogCategory, onValueChange = {}, readOnly = true, label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { category ->
                                DropdownMenuItem(text = { Text(category) }, onClick = { dialogCategory = category; expanded = false })
                            }
                        }
                    }
                    val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dialogTimestamp))
                    OutlinedTextField(value = formattedDate, onValueChange = {}, readOnly = true, label = { Text("Date") },
                        trailingIcon = { IconButton(onClick = { showDatePicker.value = true }) { Icon(Icons.Default.DateRange, "Date") } })
                    if (showAdvanced.value) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Show Pie Chart", style = MaterialTheme.typography.bodyLarge)
                            Switch(checked = isPieChartVisible.value, onCheckedChange = { isPieChartVisible.value = it })
                        }
                    }
                    if (errorMessage.isNotBlank()) {
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { showAdvanced.value = !showAdvanced.value }) {
                        Text(if (showAdvanced.value) "Hide Advanced" else "Show Advanced")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = dialogAmount.toDoubleOrNull() ?: 0.0
                    val decimalPart = dialogAmount.substringAfter(".", "")
                    if (dialogTitle.isBlank()) errorMessage = "Please enter a title"
                    else if (amt <= 0) errorMessage = "Please enter a valid amount"
                    else if (dialogAmount.contains(".") && decimalPart.length > 2) errorMessage = "Max 2 decimal places"
                    else {
                        if (editingExpense.value == null) onAddExpense(dialogTitle, amt, dialogCategory, budget.id, dialogTimestamp)
                        else onUpdateExpense(editingExpense.value!!.copy(title = dialogTitle, amount = amt, category = dialogCategory, timestamp = dialogTimestamp))
                        showEditorDialog.value = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditorDialog.value = false }) { Text("Cancel") } }
        )
    }
    
    if (showDatePicker.value) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dialogTimestamp)
        DatePickerDialog(onDismissRequest = { showDatePicker.value = false }, 
            confirmButton = { 
                TextButton(onClick = { 
                    dialogTimestamp = state.selectedDateMillis ?: dialogTimestamp
                    showDatePicker.value = false 
                }) { Text("OK") } 
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker.value = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showFilterDialog.value) {
        AlertDialog(
            onDismissRequest = { showFilterDialog.value = false },
            title = { Text("Filter by Month") },
            text = {
                Column {
                    val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
                    var tempMonth by remember { mutableIntStateOf(selectedMonth) }
                    var tempYear by remember { mutableIntStateOf(selectedYear) }
                    var monthExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = monthExpanded, onExpandedChange = { monthExpanded = it }) {
                        OutlinedTextField(
                            value = months[tempMonth], onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) }
                        )
                        ExposedDropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                            months.forEachIndexed { index, name -> DropdownMenuItem(text = { Text(name) }, onClick = { tempMonth = index; monthExpanded = false }) }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = tempYear.toString(), onValueChange = { if (it.all { c -> c.isDigit() }) tempYear = it.toIntOrNull() ?: tempYear }, label = { Text("Year") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showFilterDialog.value = false }) { Text("Cancel") }
                        TextButton(onClick = { 
                            selectedMonth = tempMonth
                            selectedYear = tempYear
                            showFilterDialog.value = false 
                        }) { Text("Apply") }
                    }
                }
            },
            confirmButton = {}, dismissButton = {}
        )
    }

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