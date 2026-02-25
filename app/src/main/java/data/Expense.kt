package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// This tells Room that its a database table
@Entity(tableName = "expenses")
data class Expense(

    // Unique ID for each expense
    // Room will auto-generate this
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // What the expense on
    val title: String,

    // the amount spent
    val amount: Double,

    // auto sets a time when it was created
    val timestamp: Long = System.currentTimeMillis()
)