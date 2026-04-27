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

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ExpenseDatabase.getDatabase(application)
    private val expenseDao = db.expenseDao()
    private val budgetDao = db.budgetDao()

    val expenses: StateFlow<List<Expense>> =
        expenseDao.getAllExpenses().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val budgets: StateFlow<List<Budget>> =
        budgetDao.getAllBudgets().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addBudget(name: String, amount: Double, endDate: Long = 0L) {
        viewModelScope.launch {
            budgetDao.insertBudget(Budget(name = name, amount = amount, endDate = endDate))
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.updateBudget(budget)
        }
    }

    fun deleteBudget(budgetId: Int) {
        viewModelScope.launch {
            budgetDao.deleteBudget(budgetId)
        }
    }

    fun addExpense(title: String, amount: Double, category: String, budgetId: Int, timestamp: Long) {
        viewModelScope.launch {
            expenseDao.insertExpense(
                Expense(
                    title = title,
                    amount = amount,
                    category = category,
                    budgetId = budgetId,
                    timestamp = timestamp
                )
            )
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.updateExpense(expense)
        }
    }

    fun deleteExpense(expenseId: Int) {
        viewModelScope.launch {
            expenseDao.deleteExpense(expenseId)
        }
    }
}