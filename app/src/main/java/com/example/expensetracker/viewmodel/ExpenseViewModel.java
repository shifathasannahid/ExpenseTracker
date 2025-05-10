package com.example.expensetracker.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.expensetracker.data.dao.ExpenseDao.CategorySum;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.data.repository.ExpenseRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ViewModel class that provides data to the UI and survives configuration changes.
 * Acts as a communication center between the Repository and the UI.
 */
public class ExpenseViewModel extends AndroidViewModel {
    
    private final ExpenseRepository repository;
    private final LiveData<List<Expense>> allExpenses;
    private final MutableLiveData<Double> monthlyBudget = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentYear = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentMonth = new MutableLiveData<>();
    
    /**
     * Constructor initializes the repository and sets default values
     * @param application Application context
     */
    public ExpenseViewModel(Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        allExpenses = repository.getAllExpenses();
        
        // Set default values for current month and year
        Calendar calendar = Calendar.getInstance();
        currentYear.setValue(calendar.get(Calendar.YEAR));
        currentMonth.setValue(calendar.get(Calendar.MONTH) + 1); // Calendar months are 0-based
        
        // Default budget (can be loaded from SharedPreferences in a real app)
        monthlyBudget.setValue(1000.0);
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
        return repository.getExpenseById(id);
    }
    
    /**
     * Get expenses by category
     * @param category Category to filter by
     * @return LiveData list of expenses in the category
     */
    public LiveData<List<Expense>> getExpensesByCategory(String category) {
        return repository.getExpensesByCategory(category);
    }
    
    /**
     * Get expenses for the current month
     * @return LiveData list of expenses for the current month
     */
    public LiveData<List<Expense>> getCurrentMonthExpenses() {
        int year = currentYear.getValue() != null ? currentYear.getValue() : Calendar.getInstance().get(Calendar.YEAR);
        int month = currentMonth.getValue() != null ? currentMonth.getValue() : Calendar.getInstance().get(Calendar.MONTH) + 1;
        
        Calendar calendar = Calendar.getInstance();
        
        // Start of month
        calendar.set(year, month - 1, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();
        
        // End of month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endDate = calendar.getTime();
        
        return repository.getExpensesBetweenDates(startDate, endDate);
    }
    
    /**
     * Get monthly expense sum for the current month
     * @return LiveData containing the sum
     */
    public LiveData<Double> getCurrentMonthExpenseSum() {
        int year = currentYear.getValue() != null ? currentYear.getValue() : Calendar.getInstance().get(Calendar.YEAR);
        int month = currentMonth.getValue() != null ? currentMonth.getValue() : Calendar.getInstance().get(Calendar.MONTH) + 1;
        return repository.getMonthlyExpenseSum(year, month);
    }
    
    /**
     * Get monthly category sums for the current month
     * @return LiveData list of category sums
     */
    public LiveData<List<CategorySum>> getCurrentMonthCategorySums() {
        int year = currentYear.getValue() != null ? currentYear.getValue() : Calendar.getInstance().get(Calendar.YEAR);
        int month = currentMonth.getValue() != null ? currentMonth.getValue() : Calendar.getInstance().get(Calendar.MONTH) + 1;
        return repository.getMonthlyCategorySums(year, month);
    }
    
    /**
     * Insert a new expense
     * @param expense Expense to insert
     * @return ID of the inserted expense
     */
    public long insert(Expense expense) {
        return repository.insert(expense);
    }
    
    /**
     * Update an existing expense
     * @param expense Expense to update
     */
    public void update(Expense expense) {
        repository.update(expense);
    }
    
    /**
     * Delete an expense
     * @param expense Expense to delete
     */
    public void delete(Expense expense) {
        repository.delete(expense);
    }
    
    /**
     * Set the monthly budget
     * @param budget Budget amount
     */
    public void setMonthlyBudget(double budget) {
        monthlyBudget.setValue(budget);
    }
    
    /**
     * Get the monthly budget
     * @return LiveData containing the budget
     */
    public LiveData<Double> getMonthlyBudget() {
        return monthlyBudget;
    }
    
    /**
     * Set the current month and year for filtering
     * @param year Year
     * @param month Month (1-12)
     */
    public void setCurrentMonthAndYear(int year, int month) {
        currentYear.setValue(year);
        currentMonth.setValue(month);
    }
    
    /**
     * Get the current year
     * @return LiveData containing the year
     */
    public LiveData<Integer> getCurrentYear() {
        return currentYear;
    }
    
    /**
     * Get the current month
     * @return LiveData containing the month
     */
    public LiveData<Integer> getCurrentMonth() {
        return currentMonth;
    }
}