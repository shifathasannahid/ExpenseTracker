package com.example.expensetracker.data.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.expensetracker.data.AppDatabase;
import com.example.expensetracker.data.dao.ExpenseDao;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.data.dao.ExpenseDao.CategorySum;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Repository class that abstracts access to the database.
 * Provides a clean API for the ViewModel to interact with the data layer.
 */
public class ExpenseRepository {
    
    private final ExpenseDao expenseDao;
    private final LiveData<List<Expense>> allExpenses;
    
    /**
     * Constructor initializes the database and DAO
     * @param application Application context
     */
    public ExpenseRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        expenseDao = database.expenseDao();
        allExpenses = expenseDao.getAllExpenses();
    }
    
    /**
     * Get all expenses
     * @return LiveData list of all expenses
     */
    public LiveData<List<Expense>> getAllExpenses() {
        return allExpenses;
    }
    
    /**
     * Get expense by ID
     * @param id Expense ID
     * @return LiveData containing the expense
     */
    public LiveData<Expense> getExpenseById(long id) {
        return expenseDao.getExpenseById(id);
    }
    
    /**
     * Get expenses by category
     * @param category Category to filter by
     * @return LiveData list of expenses in the category
     */
    public LiveData<List<Expense>> getExpensesByCategory(String category) {
        return expenseDao.getExpensesByCategory(category);
    }
    
    /**
     * Get expenses between two dates
     * @param startDate Start date
     * @param endDate End date
     * @return LiveData list of expenses between dates
     */
    public LiveData<List<Expense>> getExpensesBetweenDates(Date startDate, Date endDate) {
        return expenseDao.getExpensesBetweenDates(startDate, endDate);
    }
    
    /**
     * Get the sum of expenses for a specific month
     * @param year Year
     * @param month Month (1-12)
     * @return LiveData containing the sum of expenses for the specified month
     */
    public LiveData<Double> getMonthlyExpenseSum(int year, int month) {
        Date[] dates = getMonthStartAndEndDates(year, month);
        System.out.println("DEBUG: ExpenseRepository.getMonthlyExpenseSum - Getting expense sum for " + 
                          year + "-" + month + " (" + dates[0] + " to " + dates[1] + ")");
        
        // Get the expense sum from the DAO
        LiveData<Double> result = expenseDao.getMonthlyExpenseSum(dates[0], dates[1]);
        
        return result;
    }
    
    /**
     * Get the sum of expenses for each category in a specific month
     * @param year Year
     * @param month Month (1-12)
     * @return LiveData list of category sums
     */
    public LiveData<List<CategorySum>> getMonthlyCategorySums(int year, int month) {
        System.out.println("DEBUG: ExpenseRepository.getMonthlyCategorySums called for year: " + year + ", month: " + month);
        Date[] dates = getMonthStartAndEndDates(year, month);
        System.out.println("DEBUG: Date range for category sums: " + dates[0] + " to " + dates[1]);
        
        LiveData<List<CategorySum>> result = expenseDao.getMonthlyCategorySums(dates[0], dates[1]);
        System.out.println("DEBUG: Returning LiveData for monthly category sums");
        return result;
    }
    
    /**
     * Insert a new expense
     * @param expense Expense to insert
     * @return ID of the inserted expense
     */
    public long insert(Expense expense) {
        try {
            long id = new InsertExpenseAsyncTask(expenseDao).execute(expense).get();
            System.out.println("DEBUG: ExpenseRepository.insert - Expense inserted with ID: " + id);
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Return -1 to indicate error
        }
    }
    
    /**
     * Update an existing expense
     * @param expense Expense to update
     */
    public void update(Expense expense) {
        System.out.println("DEBUG: ExpenseRepository.update - Updating expense with ID: " + expense.getId());
        new UpdateExpenseAsyncTask(expenseDao).execute(expense);
    }
    
    /**
     * Delete an expense
     * @param expense Expense to delete
     */
    public void delete(Expense expense) {
        System.out.println("DEBUG: ExpenseRepository.delete - Deleting expense with ID: " + expense.getId());
        new DeleteExpenseAsyncTask(expenseDao).execute(expense);
    }
    
    /**
     * Helper method to get start and end dates for a month
     * @param year Year
     * @param month Month (1-12)
     * @return Array with start date at index 0 and end date at index 1
     */
    private Date[] getMonthStartAndEndDates(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        
        // Set to first day of month
        calendar.set(year, month - 1, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();
        
        // Set to last day of month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endDate = calendar.getTime();
        
        return new Date[]{startDate, endDate};
    }
    
    /**
     * AsyncTask for inserting expenses
     */
    private static class InsertExpenseAsyncTask extends AsyncTask<Expense, Void, Long> {
        private final ExpenseDao expenseDao;
        
        private InsertExpenseAsyncTask(ExpenseDao expenseDao) {
            this.expenseDao = expenseDao;
        }
        
        @Override
        protected Long doInBackground(Expense... expenses) {
            return expenseDao.insert(expenses[0]);
        }
    }
    
    /**
     * AsyncTask for updating expenses
     */
    private static class UpdateExpenseAsyncTask extends AsyncTask<Expense, Void, Void> {
        private final ExpenseDao expenseDao;
        
        private UpdateExpenseAsyncTask(ExpenseDao expenseDao) {
            this.expenseDao = expenseDao;
        }
        
        @Override
        protected Void doInBackground(Expense... expenses) {
            expenseDao.update(expenses[0]);
            return null;
        }
    }
    
    /**
     * AsyncTask for deleting expenses
     */
    private static class DeleteExpenseAsyncTask extends AsyncTask<Expense, Void, Void> {
        private final ExpenseDao expenseDao;
        
        private DeleteExpenseAsyncTask(ExpenseDao expenseDao) {
            this.expenseDao = expenseDao;
        }
        
        @Override
        protected Void doInBackground(Expense... expenses) {
            expenseDao.delete(expenses[0]);
            return null;
        }
    }
}