package com.example.expensetracker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Expense

//One row/card in the expense list Shows title, amount, Edit button, Delete button
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
            //Left side: expense info
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

            //Right side: small action buttons
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