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
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.AuthViewModel
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    // ViewModel for expenses and budgets
    private val expenseViewModel: ExpenseViewModel by viewModels()

    // ViewModel for dark/light mode
    private val themeViewModel: ThemeViewModel by viewModels()

    // ViewModel for login and create account
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExpenseTrackerTheme(darkTheme = themeViewModel.isDarkMode) {

                // Tracks which screen to show
                var showCreateAccount by remember { mutableStateOf(false) }

                // If not logged in show login or create account screen
                if (!authViewModel.isLoggedIn) {
                    if (showCreateAccount) {
                        // Show create account screen
                        CreateAccountScreen(
                            authViewModel = authViewModel,
                            onNavigateToLogin = { showCreateAccount = false }
                        )
                    } else {
                        // Show login screen
                        LoginScreen(
                            authViewModel = authViewModel,
                            onNavigateToCreateAccount = { showCreateAccount = true }
                        )
                    }
                } else {
                    // User is logged in so show the main app
                    val expenses by expenseViewModel.expenses.collectAsState()
                    val budgets by expenseViewModel.budgets.collectAsState()

                    // Tracks which budget is selected
                    var selectedBudgetId by remember { mutableStateOf<Int?>(null) }
                    val currentBudget = budgets.find { it.id == selectedBudgetId }

                    // Goes back to budget list when back button pressed
                    if (selectedBudgetId != null) {
                        BackHandler {
                            selectedBudgetId = null
                        }
                    }

                    Scaffold(
                        topBar = {
                            TopAppBar(
                                // Shows budget name or app name in the top bar
                                title = { Text(currentBudget?.name ?: "Expense Tracker") },
                                navigationIcon = {
                                    // Shows back button when inside a budget
                                    if (selectedBudgetId != null) {
                                        IconButton(onClick = { selectedBudgetId = null }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                        }
                                    }
                                },
                                actions = {
                                    // Settings button in top right
                                    SettingsMenu(
                                        isDarkMode = themeViewModel.isDarkMode,
                                        onToggleDarkMode = { themeViewModel.toggleTheme() },
                                        onLogout = { authViewModel.logout() }
                                    )
                                }
                            )
                        }
                    ) { innerPadding ->
                        // Main home screen with all budgets and expenses
                        HomeScreen(
                            expenses = expenses,
                            budgets = budgets,
                            selectedBudget = currentBudget,
                            onBudgetSelected = { selectedBudgetId = it?.id },
                            onAddBudget = { name, amount, endDate ->
                                expenseViewModel.addBudget(name, amount, endDate)
                            },
                            onUpdateBudget = { budget ->
                                expenseViewModel.updateBudget(budget)
                            },
                            onDeleteBudget = { budget ->
                                expenseViewModel.deleteBudget(budget.id)
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
}

// Settings menu in the top right corner
@Composable
fun SettingsMenu(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onLogout: () -> Unit // called when user clicks logout
) {
    // Tracks if the menu is open or closed
    var expanded by remember { mutableStateOf(false) }

    Box {
        // Settings icon button
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }

        // Dropdown menu that appears when settings is clicked
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Dark mode toggle
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("Night Mode")
                        Spacer(modifier = Modifier.width(16.dp))
                        // Toggle switch for dark mode
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onToggleDarkMode() }
                        )
                    }
                },
                onClick = { }
            )
            // Logout button
            DropdownMenuItem(
                text = { Text("Logout") },
                onClick = {
                    expanded = false
                    onLogout()
                }
            )
        }
    }
}