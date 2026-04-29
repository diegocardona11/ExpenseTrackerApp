package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// This is the Budget table in the database
@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int = 0, // tracks which user this budget belongs to
    val name: String,
    val amount: Double,
    val icon: String = "💰",
    val endDate: Long = 0L // timestamp for the budget's target end date
)