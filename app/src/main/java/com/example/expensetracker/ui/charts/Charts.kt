package com.example.expensetracker.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Expense
import com.example.expensetracker.ui.categoryColors

// Draws a pie chart showing spending by category
@Composable
fun CategoryPieChart(expenses: List<Expense>, modifier: Modifier = Modifier.Companion) {
    if (expenses.isEmpty()) return

    // Add up all spending
    val totalSpent = expenses.sumOf { it.amount }

    // Group expenses by category and add up each one
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    Box(modifier = modifier, contentAlignment = Alignment.Companion.Center) {
        Canvas(modifier = Modifier.Companion.size(120.dp)) {
            var startAngle = -90f
            // Draw each category as a slice of the pie
            categoryTotals.forEach { (category, amount) ->
                val sweepAngle = (amount / totalSpent).toFloat() * 360f
                drawArc(
                    color = categoryColors[category] ?: Color.Companion.Gray,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }
    }
}

// Shows a bar chart of spending per category
@Composable
fun CategoryBarChart(expenses: List<Expense>, modifier: Modifier = Modifier.Companion) {
    if (expenses.isEmpty()) return

    // Group expenses by category and add up each one
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    // Find the biggest amount so we can scale the bars
    val maxAmount = categoryTotals.values.maxOrNull() ?: 1.0

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Spending by Category",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Companion.Bold
        )
        Spacer(modifier = Modifier.Companion.height(8.dp))

        // Draw a bar for each category
        categoryTotals.forEach { (category, amount) ->
            Row(
                modifier = Modifier.Companion.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                // Category name on the left
                Text(
                    text = category,
                    modifier = Modifier.Companion.width(100.dp),
                    style = MaterialTheme.typography.labelSmall
                )
                // The bar itself
                LinearProgressIndicator(
                    progress = { (amount / maxAmount).toFloat() },
                    modifier = Modifier.Companion.weight(1f).height(12.dp),
                    color = categoryColors[category] ?: Color.Companion.Gray,
                    strokeCap = StrokeCap.Companion.Round
                )
                // Amount on the right
                Text(
                    text = "$${"%.0f".format(amount)}",
                    modifier = Modifier.Companion.width(50.dp).padding(start = 8.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}