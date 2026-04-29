package com.example.expensetracker.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

    // Stores the logged in user's id
    var currentUserId by mutableIntStateOf(0)

    // Shows error messages on screen
    var errorMessage by mutableStateOf("")

    // Called when user tries to login
    fun login(username: String, password: String) {
        viewModelScope.launch {
            val user = userDao.login(username, password)
            if (user != null) {
                // Save the user's id so we can use it later
                currentUserId = user.id
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
                // Insert the new user and get their id
                userDao.insertUser(User(username = username, password = password))
                val newUser = userDao.getUserByUsername(username)
                currentUserId = newUser?.id ?: 0
                isLoggedIn = true
                errorMessage = ""
            }
        }
    }

    // Logs the user out and resets everything
    fun logout() {
        isLoggedIn = false
        currentUserId = 0
        errorMessage = ""
    }
}