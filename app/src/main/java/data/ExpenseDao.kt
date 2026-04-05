package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Update

// This file is basically the Expense Helper
// It contains the functions that talk to the database
@Dao
interface ExpenseDao {

    // This saves a new expense into the database
    // the suspend makes it so it runs safely in the background with it not bugging
    @Insert
    suspend fun insertExpense(expense: Expense)

    // This updates an existing expense in the database
    @Update
    suspend fun updateExpense(expense: Expense)

    // Gets all expenses from the database
    // ORDER BY timestamp DESC means newest expenses come first
    // Flow means it automatically updates when data changes
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    // This deletes one expense using its id
    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: Int)
}