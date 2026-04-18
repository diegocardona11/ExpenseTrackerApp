@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Budget
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExpenseTrackerTheme(darkTheme = themeViewModel.isDarkMode) {

                val expenses by expenseViewModel.expenses.collectAsState()
                val budgets by expenseViewModel.budgets.collectAsState()
                
                // Track selected budget here to handle back button
                var selectedBudget by remember { mutableStateOf<Budget?>(null) }

                // This handles the physical back button on the phone
                if (selectedBudget != null) {
                    BackHandler {
                        selectedBudget = null
                    }
                }

                Scaffold(
                    topBar = {
                        // Only show the main app bar if NO budget is selected
                        if (selectedBudget == null) {
                            TopAppBar(
                                title = { Text("Expense Tracker") },
                                actions = {
                                    Switch(
                                        checked = themeViewModel.isDarkMode,
                                        onCheckedChange = { themeViewModel.toggleTheme() },
                                        modifier = Modifier.padding(end = 16.dp)
                                    )
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    HomeScreen(
                        expenses = expenses,
                        budgets = budgets,
                        selectedBudget = selectedBudget,
                        onBudgetSelected = { selectedBudget = it },
                        onAddBudget = { name, amount ->
                            expenseViewModel.addBudget(name, amount)
                        },
                        onAddExpense = { title, amount, category, budgetId, timestamp ->
                            expenseViewModel.addExpense(title, amount, category, budgetId, timestamp)
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