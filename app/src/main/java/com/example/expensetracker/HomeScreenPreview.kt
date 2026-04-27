@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.Budget

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ExpenseTrackerTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Expense Tracker") })
            }
        ) { innerPadding ->
            HomeScreen(
                expenses = emptyList<Expense>(),
                budgets = emptyList<Budget>(),
                selectedBudget = null,
                onBudgetSelected = { _ -> },
                onAddBudget = { _, _ -> },
                onUpdateBudget = { },
                onDeleteBudget = { },
                onAddExpense = { _, _, _, _, _ -> },
                onUpdateExpense = { _ -> },
                onDeleteExpense = { _ -> },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}