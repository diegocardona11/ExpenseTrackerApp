package com.example.expensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.Budget
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.ExpenseDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Handles all expense and budget logic for the app
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ExpenseDatabase.getDatabase(application)
    private val expenseDao = db.expenseDao()
    private val budgetDao = db.budgetDao()

    // Tracks the currently logged in user's id
    private var currentUserId: Int = 0

    // Holds the list of expenses for the current user
    var expenses: StateFlow<List<Expense>> = expenseDao.getAllExpenses(currentUserId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Holds the list of budgets for the current user
    var budgets: StateFlow<List<Budget>> = budgetDao.getAllBudgets(currentUserId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Called when a user logs in to load their data
    fun setUser(userId: Int) {
        currentUserId = userId
        expenses = expenseDao.getAllExpenses(userId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        budgets = budgetDao.getAllBudgets(userId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Adds a new budget for the current user
    fun addBudget(name: String, amount: Double, endDate: Long = 0L) {
        viewModelScope.launch {
            budgetDao.insertBudget(Budget(userId = currentUserId, name = name, amount = amount, endDate = endDate))
        }
    }

    // Updates an existing budget
    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.updateBudget(budget)
        }
    }

    // Deletes a budget by its id
    fun deleteBudget(budgetId: Int) {
        viewModelScope.launch {
            budgetDao.deleteBudget(budgetId)
        }
    }

    // Adds a new expense for the current user
    fun addExpense(title: String, amount: Double, category: String, budgetId: Int, timestamp: Long) {
        viewModelScope.launch {
            expenseDao.insertExpense(
                Expense(
                    userId = currentUserId,
                    title = title,
                    amount = amount,
                    category = category,
                    budgetId = budgetId,
                    timestamp = timestamp
                )
            )
        }
    }

    // Updates an existing expense
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.updateExpense(expense)
        }
    }

    // Deletes an expense by its id
    fun deleteExpense(expenseId: Int) {
        viewModelScope.launch {
            expenseDao.deleteExpense(expenseId)
        }
    }
}