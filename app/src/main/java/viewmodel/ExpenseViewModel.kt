package com.example.expensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.ExpenseDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel is our brain
// It talks to the database and gives the UI the data it needs
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    // allows acess to the database plus the DAO whish is the helper
    private val dao = ExpenseDatabase.getDatabase(application).expenseDao()

    // This is the list of expenses the UI will display
    // StateFlow updates automatically when the database changes
    val expenses: StateFlow<List<Expense>> =
        dao.getAllExpenses().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // saves an expense to the database
    fun addExpense(title: String, amount: Double) {
        viewModelScope.launch {
            dao.insertExpense(
                Expense(
                    title = title,
                    amount = amount
                )
            )
        }
    }

    // deletes an expense from the database
    fun deleteExpense(expenseId: Int) {
        viewModelScope.launch {
            dao.deleteExpense(expenseId)
        }
    }
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            dao.updateExpense(expense)
        }
    }
}