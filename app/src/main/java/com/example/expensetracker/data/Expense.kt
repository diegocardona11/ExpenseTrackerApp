package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val budgetId: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val budget: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)