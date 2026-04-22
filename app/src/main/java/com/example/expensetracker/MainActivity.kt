@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
                
                var selectedBudget by remember { mutableStateOf<Budget?>(null) }

                if (selectedBudget != null) {
                    BackHandler {
                        selectedBudget = null
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(selectedBudget?.name ?: "Expense Tracker") },
                            navigationIcon = {
                                if (selectedBudget != null) {
                                    IconButton(onClick = { selectedBudget = null }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            },
                            actions = {
                                SettingsMenu(
                                    isDarkMode = themeViewModel.isDarkMode,
                                    onToggleDarkMode = { themeViewModel.toggleTheme() }
                                )
                            }
                        )
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

@Composable
fun SettingsMenu(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("Night Mode")
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onToggleDarkMode() }
                        )
                    }
                },
                onClick = { /* Switch handles the interaction */ }
            )
        }
    }
}