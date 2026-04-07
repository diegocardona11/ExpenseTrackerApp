package com.example.expensetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// This tells Room which tables belong to the database
@Database(
    entities = [Expense::class], // our table
    version = 2 // changed from 1 to 2 because we added category
)
abstract class ExpenseDatabase : RoomDatabase() {

    // This connects the database to our helper (DAO)
    abstract fun expenseDao(): ExpenseDao

    companion object {

        // This makes sure we only create one database
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        // This safely gives us the database
        fun getDatabase(context: Context): ExpenseDatabase {

            // If database already exists, use it
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_database"
                )
                    // If the database structure changes, rebuild it
                    // This is okay for our student project
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}