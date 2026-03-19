@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.expensetracker


import android.os.Bundle

// Activity and Compose setup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

// Material 3 UI components
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

// Compose state tools
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

// UI helpers
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier

// Your app's theme and ViewModel
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {

    //This gets the ViewModel for this screen.
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Makes the app draw edge-to-edge nicely on modern Android screens.
        enableEdgeToEdge()

        // setContent starts Jetpack Compose UI.
        setContent {
            ExpenseTrackerTheme {

                val expenses by viewModel.expenses.collectAsState()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Expense Tracker") }
                        )
                    }
                ) { innerPadding ->
                    HomeScreen(
                        expenses = expenses,
                        onAddExpense = { title, amount ->
                            // Calls ViewModel to add a new expense into Room
                            viewModel.addExpense(title, amount)
                        },
                        onDeleteExpense = { expense ->
                            // Deletes by id, because ViewModel delete function expects an Int id
                            viewModel.deleteExpense(expense.id)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}