package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// This talks to the database for user related stuff
@Dao
interface UserDao {

    // Saves a new user to the database
    @Insert
    suspend fun insertUser(user: User)

    // Finds a user by their username and password for login
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): User?

    // Checks if a username is already taken
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
}