package com.example.expensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope // this lets us run code in background safely
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.ExpenseDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// This is the "brain" of the app
// It connects the UI to the database
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    // This gets access to the database + helper (DAO)
    private val dao = ExpenseDatabase.getDatabase(application).expenseDao()

    // This holds the list of expenses for the UI
    // It automatically updates when the database changes
    val expenses: StateFlow<List<Expense>> =
        dao.getAllExpenses().stateIn(
            scope = viewModelScope, // runs safely in background
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // starts empty
        )

    // ADD EXPENSE
    // Saves a new expense into the database
    fun addExpense(title: String, amount: Double, category: String) {
        viewModelScope.launch {
            dao.insertExpense(
                Expense(
                    title = title,
                    amount = amount,
                    category = category // new field we added
                )
            )
        }
    }

    // DELETE EXPENSE
    // Deletes an expense using its ID
    fun deleteExpense(expenseId: Int) {
        viewModelScope.launch {
            dao.deleteExpense(expenseId)
        }
    }

    // UPDATE EXPENSE
    // Updates an existing expense (used when editing)
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            dao.updateExpense(expense)
        }
    }
}