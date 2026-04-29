package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// This is the Expense table in the database
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val budgetId: Int,
    val userId: Int = 0, // tracks which user this expense belongs to
    val title: String,
    val amount: Double,
    val category: String,
    val budget: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)