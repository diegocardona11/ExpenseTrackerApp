package com.example.expensetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// This is the main database for the app
// We added User::class and bumped version to 6
@Database(
    entities = [Expense::class, Budget::class, User::class],
    version = 6
)
abstract class ExpenseDatabase : RoomDatabase() {

    // Gives access to expense related database functions
    abstract fun expenseDao(): ExpenseDao

    // Gives access to budget related database functions
    abstract fun budgetDao(): BudgetDao

    // Gives access to user related database functions
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        // Creates the database if it doesn't exist yet
        fun getDatabase(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_database"
                )
                    // Clears the database if the version changes
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}