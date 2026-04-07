@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import android.os.Bundle

// Activity + Compose setup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

// Compose layout helper
import androidx.compose.foundation.layout.padding

// Material 3 UI
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

// Compose state
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

// UI helper
import androidx.compose.ui.Modifier

// Theme + ViewModel
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {

    // This gives us access to the ViewModel
    // The ViewModel talks to the database for us
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Makes the app draw nicely edge-to-edge
        enableEdgeToEdge()

        // Starts the Compose UI
        setContent {
            ExpenseTrackerTheme {

                // Gets the live list of expenses from the ViewModel
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

                        // ➕ Adds a new expense with title, amount, and category
                        onAddExpense = { title, amount, category ->
                            viewModel.addExpense(title, amount, category)
                        },

                        // ✏️ Updates an existing expense
                        onUpdateExpense = { expense ->
                            viewModel.updateExpense(expense)
                        },

                        // 🗑 Deletes an expense using its id
                        onDeleteExpense = { expense ->
                            viewModel.deleteExpense(expense.id)
                        },

                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}