package com.example.expensetracker.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.expensetracker.data.dao.ExpenseDao;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.util.DateConverter;

/**
 * Main database class for the application.
 * Defines the database configuration and serves as the main access point for the database.
 */
@Database(entities = {Expense.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "expense_tracker_db";
    private static volatile AppDatabase INSTANCE;
    
    /**
     * Get the ExpenseDao for database operations
     * @return ExpenseDao instance
     */
    public abstract ExpenseDao expenseDao();
    
    /**
     * Get the database instance (singleton pattern)
     * @param context Application context
     * @return AppDatabase instance
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                            .fallbackToDestructiveMigration() // For simplicity in development
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}