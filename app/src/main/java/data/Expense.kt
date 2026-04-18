package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// This tells Room that this class is a database table
@Entity(tableName = "expenses")
data class Expense(

    // Unique ID for each expense
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    // Links this expense to a specific budget
    val budgetId: Int,

    // What the expense was for
    val title: String,

    // How much money was spent
    val amount: Double,

    // Category like Food, Gas, Entertainment, etc.
    val category: String,

    // Optional budget amount for this specific category (keeping for detail compatibility)
    val budget: Double = 0.0,

    // Automatically saves the time the expense was created
    val timestamp: Long = System.currentTimeMillis()
)