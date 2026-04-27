package com.example.expensetracker.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.ExpenseDatabase
import com.example.expensetracker.data.User
import kotlinx.coroutines.launch

// Handles all the login and create account logic
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = ExpenseDatabase.getDatabase(application).userDao()

    // Tracks if the user is logged in
    var isLoggedIn by mutableStateOf(false)

    // Shows error messages on screen
    var errorMessage by mutableStateOf("")

    // Called when user tries to login
    fun login(username: String, password: String) {
        viewModelScope.launch {
            val user = userDao.login(username, password)
            if (user != null) {
                isLoggedIn = true
                errorMessage = ""
            } else {
                errorMessage = "Wrong username or password"
            }
        }
    }

    // Called when user tries to create an account
    fun createAccount(username: String, password: String) {
        viewModelScope.launch {
            val existing = userDao.getUserByUsername(username)
            if (existing != null) {
                errorMessage = "Username already taken"
            } else {
                userDao.insertUser(User(username = username, password = password))
                isLoggedIn = true
                errorMessage = ""
            }
        }
    }
}