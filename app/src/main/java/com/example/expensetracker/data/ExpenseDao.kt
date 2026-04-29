package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// This talks to the database for expense related stuff
@Dao
interface ExpenseDao {

    // Saves a new expense to the database
    @Insert
    suspend fun insertExpense(expense: Expense)

    // Updates an existing expense
    @Update
    suspend fun updateExpense(expense: Expense)

    // Gets all expenses for a specific user only
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllExpenses(userId: Int): Flow<List<Expense>>

    // Deletes an expense by its id
    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: Int)
}