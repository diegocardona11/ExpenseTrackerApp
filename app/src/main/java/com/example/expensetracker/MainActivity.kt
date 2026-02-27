@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.expensetracker

// Android basics
import android.os.Bundle

// Activity and Compose setup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

// Compose layout tools
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

// Keyboard option for numeric input
import androidx.compose.foundation.text.KeyboardOptions

// Material 3 UI components
import androidx.compose.material3.*

// Compose state tools
import androidx.compose.runtime.*

// UI helpers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Your app's data model, theme, and ViewModel
import com.example.expensetracker.data.Expense
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.ExpenseViewModel

/*
    MainActivity is the entry point of the app.

    This is the screen Android launches when the app opens.
    It sets up the UI and connects the UI to the ViewModel.
*/
class MainActivity : ComponentActivity() {

    /*
        This gets the ViewModel for this screen.

        Why we use a ViewModel:
        - It holds app data/state for the UI.
        - It survives simple screen changes better than raw local variables.
        - It acts as the middleman between the UI and the Room database.

        In this app, the ViewModel gives us:
        - the list of expenses
        - functions to add expenses
        - functions to delete expenses
    */
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Makes the app draw edge-to-edge nicely on modern Android screens.
        enableEdgeToEdge()

        /*
            setContent starts Jetpack Compose UI.

            Everything inside this block is Compose UI code.
            Think of it as: "build my whole screen here".
        */
        setContent {
            // Apply your app theme (colors, typography, etc.)
            ExpenseTrackerTheme {

                /*
                    Collect expenses from the ViewModel.

                    viewModel.expenses is likely a Flow or StateFlow from Room.
                    collectAsState() turns that into Compose state.

                    That means:
                    - whenever the database changes
                    - Compose automatically updates the screen
                    - the UI always stays in sync with the stored data
                */
                val expenses by viewModel.expenses.collectAsState()

                /*
                    Scaffold is a basic screen layout container from Material.

                    It helps structure a screen with parts like:
                    - top bar
                    - bottom bar
                    - floating action button
                    - content area

                    Here we use it mostly for the top app bar.
                */
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Expense Tracker") }
                        )
                    }
                ) { innerPadding ->

                    /*
                        Pass the expense data + actions into HomeScreen.

                        HomeScreen is the actual body/content of the page.

                        We pass:
                        - expenses = current list from DB
                        - onAddExpense = what happens when user adds one
                        - onDeleteExpense = what happens when user deletes one
                        - modifier padding from Scaffold so content doesn't overlap the top bar
                    */
                    HomeScreen(
                        expenses = expenses,
                        onAddExpense = { title, amount ->
                            // Calls your ViewModel to add a new expense into Room
                            viewModel.addExpense(title, amount)
                        },
                        onDeleteExpense = { expense ->
                            // Deletes by id, because your ViewModel delete function expects an Int id
                            viewModel.deleteExpense(expense.id)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/*
    HomeScreen is the main UI content shown inside MainActivity.

    It shows:
    - monthly total
    - Add Expense button
    - expense list
    - edit popup
    - delete confirmation popup
*/
@Composable
fun HomeScreen(
    expenses: List<Expense>,
    onAddExpense: (String, Double) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    /*
        Compute the total of all expenses.

        sumOf adds every expense.amount together.
        This updates automatically when the expense list changes.
    */
    val total = expenses.sumOf { it.amount }

    /*
        These are local UI state variables.

        They are NOT directly the database.
        They control temporary things on the screen, like:
        - what's typed in the popup
        - whether an item is being edited
        - whether to show dialogs
        - current error message

        remember { mutableStateOf(...) } means:
        - keep this value in Compose state
        - redraw the UI whenever it changes
    */

    // Title typed into the Add/Edit popup
    var dialogTitle by remember { mutableStateOf("") }

    // Amount typed into the Add/Edit popup
    var dialogAmount by remember { mutableStateOf("") }

    // Red validation message shown inside the popup if input is bad
    var errorMessage by remember { mutableStateOf("") }

    /*
        If this is null:
        - we are adding a brand new expense

        If this has an Expense:
        - we are editing that expense
    */
    var editingExpense by remember { mutableStateOf<Expense?>(null) }

    /*
        If this is null:
        - no delete confirmation dialog is showing

        If this has an Expense:
        - show the delete confirmation popup for that expense
    */
    var deleteTarget by remember { mutableStateOf<Expense?>(null) }

    /*
        Controls whether the Add/Edit popup is visible.

        false = popup hidden
        true = popup visible
    */
    var showEditorDialog by remember { mutableStateOf(false) }

    /*
        Main vertical layout for the screen.
    */
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        /*
            Top summary card showing total expenses.
        */
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "This Month",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                /*
                    Format total to 2 decimal places for money display.
                    Example:
                    12 -> 12.00
                    12.5 -> 12.50
                */
                Text(
                    "$${"%.2f".format(total)}",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /*
            Main add button.

            This no longer has the text fields directly on the screen.
            Instead, it opens a popup (AlertDialog).

            When pressed:
            - clear any previous edit mode
            - clear text fields
            - clear errors
            - open the popup
        */
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

        /*
            If there are no expenses, show a friendly empty-state message.
            Otherwise, show the list.
        */
        if (expenses.isEmpty()) {
            Text("No expenses yet")
        } else {
            /*
                LazyColumn = a scrolling list.
                It only renders visible items efficiently.
            */
            LazyColumn {
                items(expenses) { expense ->
                    ExpenseRow(
                        expense = expense,

                        /*
                            Edit button behavior:
                            - store which expense is being edited
                            - fill popup fields with current values
                            - clear errors
                            - open popup
                        */
                        onEdit = {
                            editingExpense = expense
                            dialogTitle = expense.title
                            dialogAmount = expense.amount.toString()
                            errorMessage = ""
                            showEditorDialog = true
                        },

                        /*
                            Delete button behavior:
                            - don't delete immediately
                            - instead, set deleteTarget
                            - that makes the confirmation dialog appear
                        */
                        onDelete = {
                            deleteTarget = expense
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        /*
            Add/Edit popup.

            This shows when showEditorDialog == true.

            We use the same popup for:
            - adding a new expense
            - editing an old expense

            The difference is whether editingExpense is null.
        */
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
                        /*
                            Expense title input field.
                        */
                        OutlinedTextField(
                            value = dialogTitle,
                            onValueChange = { dialogTitle = it },
                            label = { Text("Expense Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        /*
                            Amount input field.

                            keyboardOptions = number keyboard
                            so mobile shows a more numeric-friendly keyboard.
                        */
                        OutlinedTextField(
                            value = dialogAmount,
                            onValueChange = { dialogAmount = it },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        /*
                            If there's an error, show it in red under the fields.
                        */
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
                            /*
                                Try converting amount text into a Double.
                                If conversion fails, amount becomes null.
                            */
                            val amount = dialogAmount.toDoubleOrNull()

                            /*
                                Check decimal places manually.

                                Example:
                                "12.34" -> decimalPart = "34" -> valid
                                "12.999" -> decimalPart = "999" -> invalid
                            */
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
                                /*
                                    If editing:
                                    - delete old expense first
                                    - then add the new updated version

                                    This is a simple student-friendly edit approach.
                                    Not the fanciest, but it works with your current setup.
                                */
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

        /*
            Delete confirmation popup.

            This only shows when deleteTarget is not null.
            We use let {} so we can safely access the selected expense.
        */
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

/*
    One row/card in the expense list.

    Shows:
    - title
    - amount
    - Edit button
    - Delete button
*/
@Composable
fun ExpenseRow(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),

            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            /*
                Left side: expense info
            */
            Column {
                Text(
                    expense.title,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    "$${"%.2f".format(expense.amount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            /*
                Right side: small action buttons
            */
            Row {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }

                OutlinedButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}

/*
    Preview for Android Studio design preview.

    This is just for editor preview.
    It does not affect the real app database.
*/
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ExpenseTrackerTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Expense Tracker") })
            }
        ) { padding ->
            HomeScreen(
                expenses = emptyList(),
                onAddExpense = { _, _ -> },
                onDeleteExpense = {},
                modifier = Modifier.padding(padding)
            )
        }
    }
}