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

    // Gets a user by their id
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    // Changes the password for a specific user
    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    suspend fun changePassword(userId: Int, newPassword: String)
}