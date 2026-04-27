package com.example.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ThemeViewModel : ViewModel() {
    // This variable keeps track of whether dark mode is on or off
    var isDarkMode by mutableStateOf(false)
        private set

    // This function flips the switch
    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }
}