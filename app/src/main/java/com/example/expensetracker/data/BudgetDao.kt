package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// This talks to the database for budget related stuff
@Dao
interface BudgetDao {

    // Saves a new budget to the database
    @Insert
    suspend fun insertBudget(budget: Budget)

    // Updates an existing budget
    @Update
    suspend fun updateBudget(budget: Budget)

    // Gets all budgets for a specific user only
    @Query("SELECT * FROM budgets WHERE userId = :userId")
    fun getAllBudgets(userId: Int): Flow<List<Budget>>

    // Deletes a budget by its id
    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteBudget(budgetId: Int)
}