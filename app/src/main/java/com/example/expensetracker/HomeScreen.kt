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
    onAddExpense: (String, Double) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {

    //Compute the total of all expenses.
    val total = expenses.sumOf { it.amount }

    //These are local UI state variables.

    // Title typed into the Add/Edit popup
    var dialogTitle by remember { mutableStateOf("") }

    // Amount typed into the Add/Edit popup
    var dialogAmount by remember { mutableStateOf("") }

    // Red validation message shown inside the popup if input is bad
    var errorMessage by remember { mutableStateOf("") }

    var editingExpense by remember { mutableStateOf<Expense?>(null) }

    var deleteTarget by remember { mutableStateOf<Expense?>(null) }

    var showEditorDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        //Top summary card showing total expenses.
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

        Button(
            onClick = {
                editingExpense = null
                dialogTitle = ""
                dialogAmount = ""
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

                        //Edit button behavior:
                        onEdit = {
                            editingExpense = expense
                            dialogTitle = expense.title
                            dialogAmount = expense.amount.toString()
                            errorMessage = ""
                            showEditorDialog = true
                        },

                        //Delete button behavior:
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
                    // User tapped outside or dismissed dialog
                    showEditorDialog = false
                    errorMessage = ""
                },

                title = {
                    // Dynamic title depending on mode
                    Text(
                        if (editingExpense == null) "Add Expense"
                        else "Edit Expense"
                    )
                },

                text = {
                    Column {
                        OutlinedTextField(
                            value = dialogTitle,
                            onValueChange = { dialogTitle = it },
                            label = { Text("Expense Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = dialogAmount,
                            onValueChange = { dialogAmount = it },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // If there's an error, show it in red under the fields.
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

                            /*
                                Validation rules in order:
                                1. Title can't be blank
                                2. Amount must be a real number
                                3. Max 2 decimal places
                                4. Must be greater than 0
                            */
                            if (dialogTitle.isBlank()) {
                                errorMessage = "Please enter a title"
                            } else if (amount == null) {
                                errorMessage = "Please enter a valid number"
                            } else if (hasTooManyDecimals) {
                                errorMessage = "Use no more than 2 decimal places"
                            } else if (amount <= 0) {
                                errorMessage = "Amount must be greater than 0"
                            } else {

                                //If editing delete old expense first, then add the new updated version
                                editingExpense?.let {
                                    onDeleteExpense(it)
                                }

                                // Add the new or updated expense
                                onAddExpense(dialogTitle, amount)

                                // Reset popup state after success
                                dialogTitle = ""
                                dialogAmount = ""
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
                            // Cancel closes popup and clears mode/errors
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
                            // Actually delete it now
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