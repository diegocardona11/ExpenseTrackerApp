@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExpenseTrackerTheme {
                // Scaffold gives us a basic screen structure (top bar + content)
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Expense Tracker") })
                    }
                ) { innerPadding ->
                    // This is our "Home Screen"
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * Home screen = what the user sees first.
 * For now we use sample data. Later we connect Room database.
 */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {

    // Sample expenses (temporary)
    val sampleExpenses = listOf(
        ExpenseItem("Lunch", 12.50),
        ExpenseItem("Gas", 40.00),
        ExpenseItem("Coffee", 5.25)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Big total card (temporary total)
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("This Month", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("$57.75", style = MaterialTheme.typography.headlineMedium) // placeholder
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Expense button (we’ll connect this to a new screen next)
        Button(
            onClick = { /* TODO: Navigate to Add Expense screen */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Add Expense")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Recent Expenses", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // List of expenses
        LazyColumn {
            items(sampleExpenses) { expense ->
                ExpenseRow(expense)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * One row in the list
 */
@Composable
fun ExpenseRow(expense: ExpenseItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(expense.title, style = MaterialTheme.typography.bodyLarge)
            Text("$${"%.2f".format(expense.amount)}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// Simple data model (temporary). Later this becomes a Room Entity.
data class ExpenseItem(
    val title: String,
    val amount: Double
)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ExpenseTrackerTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("Expense Tracker") }) }) { padding ->
            HomeScreen(modifier = Modifier.padding(padding))
        }
    }
}