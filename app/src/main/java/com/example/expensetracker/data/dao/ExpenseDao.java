package com.example.expensetracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expensetracker.data.entity.Expense;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) for Expense entity.
 * Provides methods to interact with the expenses table in the database.
 */
@Dao
public interface ExpenseDao {
    
    /**
     * Insert a new expense into the database
     * @param expense The expense to be inserted
     * @return The row ID of the newly inserted expense
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Expense expense);
    
    /**
     * Update an existing expense in the database
     * @param expense The expense to be updated
     */
    @Update
    void update(Expense expense);
    
    /**
     * Delete an expense from the database
     * @param expense The expense to be deleted
     */
    @Delete
    void delete(Expense expense);
    
    /**
     * Get all expenses from the database
     * @return LiveData list of all expenses
     */
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    LiveData<List<Expense>> getAllExpenses();
    
    /**
     * Get an expense by its ID
     * @param id The ID of the expense
     * @return The expense with the specified ID
     */
    @Query("SELECT * FROM expenses WHERE id = :id")
    LiveData<Expense> getExpenseById(long id);
    
    /**
     * Get expenses for a specific category
     * @param category The category to filter by
     * @return LiveData list of expenses in the specified category
     */
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesByCategory(String category);
    
    /**
     * Get expenses between two dates
     * @param startDate The start date
     * @param endDate The end date
     * @return LiveData list of expenses between the specified dates
     */
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesBetweenDates(Date startDate, Date endDate);
    
    /**
     * Get the sum of expenses for a specific month and year
     * @param startDate The start date of the month
     * @param endDate The end date of the month
     * @return The sum of expenses for the specified month
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    LiveData<Double> getMonthlyExpenseSum(Date startDate, Date endDate);
    
    /**
     * Get the sum of expenses for each category in a specific month
     * @param startDate The start date of the month
     * @param endDate The end date of the month
     * @return List of category and sum pairs
     */
    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE date BETWEEN :startDate AND :endDate GROUP BY category")
    LiveData<List<CategorySum>> getMonthlyCategorySums(Date startDate, Date endDate);
    
    /**
     * Static class to hold category sum results
     */
    class CategorySum {
        public String category;
        public double total;
        
        public CategorySum(String category, double total) {
            this.category = category;
            this.total = total;
        }
    }
}