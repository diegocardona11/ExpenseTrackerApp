@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    // ViewModels for managing data and theme state
    private val expenseViewModel: ExpenseViewModel by viewModels()
    //Them Vew Model heeps track if dark mode is on or off
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Here we actually apply the them from themeViewModel based on its state
            ExpenseTrackerTheme(darkTheme = themeViewModel.isDarkMode) {

                val expenses by expenseViewModel.expenses.collectAsState()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Expense Tracker") },
                            actions = {
                                // here is the actual switch logic
                                Switch(
                                    checked = themeViewModel.isDarkMode,
                                    onCheckedChange = { themeViewModel.toggleTheme() },
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            }
                        )
                    }
                ) { innerPadding ->
                    HomeScreen(
                        expenses = expenses,
                        onAddExpense = { title, amount, category ->
                            expenseViewModel.addExpense(title, amount, category)
                        },
                        onUpdateExpense = { expense ->
                            expenseViewModel.updateExpense(expense)
                        },
                        onDeleteExpense = { expense ->
                            expenseViewModel.deleteExpense(expense.id)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}