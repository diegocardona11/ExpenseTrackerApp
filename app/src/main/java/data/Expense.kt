package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// This tells Room that this class is a database table
@Entity(tableName = "expenses")
data class Expense(

    // Unique ID for each expense
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // What the expense was for
    val title: String,

    // How much money was spent
    val amount: Double,

    // Category like Food, Gas, Entertainment, etc.
    val category: String,

    // Automatically saves the time the expense was created
    val timestamp: Long = System.currentTimeMillis()
)