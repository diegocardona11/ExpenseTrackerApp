package com.example.expensetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Communicates with Room about what tables belong to the database
@Database(
    entities = [Expense::class], // The table
    version = 3
)
abstract class ExpenseDatabase : RoomDatabase() {

    // This connects the database to the DAO (helper)
    abstract fun expenseDao(): ExpenseDao

    companion object {

        // This makes sure we only create one database
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        // This function gives us the database safely
        fun getDatabase(context: Context): ExpenseDatabase {

            // If database already exists → return it
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_database" // Name of the database file
                )
                .fallbackToDestructiveMigration(dropAllTables = true) // Wipes and rebuilds database if schema changes
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}