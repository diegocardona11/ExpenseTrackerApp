package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// This is the User table in the database
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // auto generated id for each user
    val username: String, // the username they pick
    val password: String // the password they pick
)