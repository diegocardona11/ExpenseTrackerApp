package com.example.expensetracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Expense

@Composable
fun HomeScreen(
    expenses: List<Expense>,
    onAddExpense: (String, Double, String) -> Unit,
    onUpdateExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {

    // Adds up all expense amounts for the total card
    val total = expenses.sumOf { it.amount }

    // Title typed into the popup
    var dialogTitle by remember { mutableStateOf("") }

    // Amount typed into the popup
    var dialogAmount by remember { mutableStateOf("") }

    // Category typed into the popup
    var dialogCategory by remember { mutableStateOf("Food") }

    // Error message shown if the input is bad
    var errorMessage by remember { mutableStateOf("") }

    // If this is null, we are adding a new expense
    // If this has a value, we are editing an old one
    var editingExpense by remember { mutableStateOf<Expense?>(null) }

    // This stores the expense the user wants to delete
    var deleteTarget by remember { mutableStateOf<Expense?>(null) }

    // Controls whether the add/edit popup is visible
    var showEditorDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top card showing the total amount
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "This Month",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "$${"%.2f".format(total)}",
                    style = MaterialTheme.typography.headlineMedium
                )
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
                errorMessage = ""
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
                            errorMessage = ""
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
                            onValueChange = { dialogAmount = it },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category input
                        OutlinedTextField(
                            value = dialogCategory,
                            onValueChange = { dialogCategory = it },
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth()
                        )

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
                            val decimalPart = dialogAmount.substringAfter(".", "")
                            val hasTooManyDecimals = decimalPart.length > 2

                            // Basic validation
                            if (dialogTitle.isBlank()) {
                                errorMessage = "Please enter a title"
                            } else if (amount == null) {
                                errorMessage = "Please enter a valid number"
                            } else if (hasTooManyDecimals) {
                                errorMessage = "Use no more than 2 decimal places"
                            } else if (amount <= 0) {
                                errorMessage = "Amount must be greater than 0"
                            } else if (dialogCategory.isBlank()) {
                                errorMessage = "Please enter a category"
                            } else {

                                if (editingExpense == null) {
                                    // Add a brand new expense
                                    onAddExpense(dialogTitle, amount, dialogCategory)
                                } else {
                                    // Update the existing expense
                                    onUpdateExpense(
                                        editingExpense!!.copy(
                                            title = dialogTitle,
                                            amount = amount,
                                            category = dialogCategory
                                        )
                                    )
                                }

                                // Reset popup after success
                                dialogTitle = ""
                                dialogAmount = ""
                                dialogCategory = "Food"
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