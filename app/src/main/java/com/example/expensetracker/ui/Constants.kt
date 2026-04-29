package com.example.expensetracker.ui

import androidx.compose.ui.graphics.Color

// List of expense categories for the dropdown
val categories = listOf(
    "Housing", "Food", "Transportation", "Gas", "Bills", "Shopping",
    "Entertainment", "Health", "Education", "Travel", "Savings", "Other"
)

// Colors for each category used in charts
val categoryColors = mapOf(
    "Housing" to Color(0xFFE57373),
    "Food" to Color(0xFF81C784),
    "Transportation" to Color(0xFF64B5F6),
    "Gas" to Color(0xFFFFD54F),
    "Bills" to Color(0xFFBA68C8),
    "Shopping" to Color(0xFFF06292),
    "Entertainment" to Color(0xFF4DB6AC),
    "Health" to Color(0xFFFF8A65),
    "Education" to Color(0xFF9575CD),
    "Travel" to Color(0xFFAED581),
    "Savings" to Color(0xFFFFB74D),
    "Other" to Color(0xFF90A4AE)
)

// The different time range options the user can pick
enum class TimeRange(val label: String) {
    ONE_WEEK("1 Week"),
    TWO_WEEKS("2 Weeks"),
    MONTHLY("Monthly")
}