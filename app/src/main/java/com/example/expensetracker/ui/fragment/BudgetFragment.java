package com.example.expensetracker.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.expensetracker.R;
import com.example.expensetracker.viewmodel.ExpenseViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.util.Log;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Fragment for setting and tracking monthly budget.
 * Allows users to set a budget amount and displays progress towards that budget.
 */
public class BudgetFragment extends Fragment {
    private static final String TAG = "BudgetFragment";

    private ExpenseViewModel expenseViewModel;
    private TextInputLayout textInputLayoutBudget;
    private TextInputEditText editTextBudget;
    private Button buttonSaveBudget;
    private TextView textViewCurrentBudget;
    private TextView textViewCurrentSpending;
    private TextView textViewRemaining;
    private LinearProgressIndicator progressIndicator;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("bn", "BD"));
    
    /**
     * Safely sets text on a TextView with null check
     * @param textView The TextView to update
     * @param text The text to set
     * @return true if successful, false if textView was null
     */
    private boolean safeSetText(TextView textView, String text) {
        if (textView != null) {
            textView.setText(text);
            return true;
        }
        return false;
    }
    
    /**
     * Safely sets color on a View with null check
     * @param view The View to update
     * @param color The color to set
     * @return true if successful, false if view was null
     */
    private boolean safeSetColor(View view, int color) {
        if (view != null) {
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(color);
            } else if (view instanceof LinearProgressIndicator) {
                ((LinearProgressIndicator) view).setIndicatorColor(color);
            }
            view.invalidate();
            return true;
        }
        return false;
    }
    
    /**
     * Safely sets progress on a progress indicator with null check
     * @param progressIndicator The progress indicator to update
     * @param progress The progress value to set (0-100)
     * @return true if successful, false if progressIndicator was null
     */
    private boolean safeSetProgress(LinearProgressIndicator progressIndicator, int progress) {
        if (progressIndicator != null) {
            // Ensure progress is between 0 and 100
            int cappedProgress = Math.min(Math.max(progress, 0), 100);
            progressIndicator.setProgress(cappedProgress);
            safeRequestLayout(progressIndicator);
            return true;
        }
        return false;
    }
    
    /**
     * Safely requests layout update on a view with null check
     * @param view The view to update layout for
     * @return true if successful, false if view was null
     */
    private boolean safeRequestLayout(View view) {
        if (view != null) {
            view.requestLayout();
            view.invalidate();
            return true;
        }
        return false;
    }
    
    /**
     * Verifies that the budget calculation logic is correct (budget - spent = remaining)
     * @param budget The budget amount
     * @param spent The amount spent
     * @param remaining The calculated remaining amount
     */
    private void verifyBudgetCalculation(double budget, double spent, double remaining) {
        // Due to floating point precision, we'll use a small epsilon for comparison
        double epsilon = 0.001;
        double calculatedRemaining = budget - spent;
        
        if (Math.abs(calculatedRemaining - remaining) > epsilon) {
            Log.e(TAG, "Budget calculation error: budget(" + budget + ") - spent(" + spent + ") = " + 
                  calculatedRemaining + ", but remaining = " + remaining);
        } else {
            Log.d(TAG, "Budget calculation verified: " + budget + " - " + spent + " = " + remaining);
        }
    }
    
    /**
     * Formats a double value as currency using the current locale
     * @param value The value to format
     * @return The formatted currency string
     */
    private String formatCurrency(double value) {
        // Use the existing currencyFormat which is already initialized as final
        return currencyFormat.format(value);
    }
    
    /**
     * Safely shows a toast message with null check for context
     * @param message The message to show
     * @param duration The toast duration (Toast.LENGTH_SHORT or Toast.LENGTH_LONG)
     * @return true if successful, false if context was null
     */
    private boolean safeShowToast(String message, int duration) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, duration).show();
            return true;
        }
        Log.d(TAG, "Cannot show toast: " + message + " (context is null)");
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        textInputLayoutBudget = view.findViewById(R.id.text_input_layout_budget);
        editTextBudget = view.findViewById(R.id.edit_text_budget);
        buttonSaveBudget = view.findViewById(R.id.button_save_budget);
        textViewCurrentBudget = view.findViewById(R.id.text_view_current_budget);
        textViewCurrentSpending = view.findViewById(R.id.text_view_current_spending);
        textViewRemaining = view.findViewById(R.id.text_view_remaining);
        progressIndicator = view.findViewById(R.id.progress_indicator);

        // Set up ViewModel
        expenseViewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        // Set up budget save button
        buttonSaveBudget.setOnClickListener(v -> saveBudget());

        Log.d(TAG, "Setting up LiveData observers");
        
        // Observe budget changes
        expenseViewModel.getMonthlyBudget().observe(getViewLifecycleOwner(), budget -> {
            Log.d(TAG, "Budget LiveData updated: " + budget);
            // Don't update the UI directly here, let updateBudgetUI handle it
            // This ensures consistent calculation across all updates
            updateBudgetUI();
        });

        // Observe current month expenses
        expenseViewModel.getCurrentMonthExpenseSum().observe(getViewLifecycleOwner(), sum -> {
            Log.d(TAG, "Expense sum LiveData updated: " + sum);
            // Don't update the UI directly here, let updateBudgetUI handle it
            // This ensures consistent calculation across all updates
            updateBudgetUI();
        });
        
        // Observe all expenses to ensure we update when expenses are added/modified
        expenseViewModel.getAllExpenses().observe(getViewLifecycleOwner(), expenses -> {
            Log.d(TAG, "All expenses LiveData updated with " + (expenses != null ? expenses.size() : 0) + " items");
            // Force a refresh of the current month expense sum
            expenseViewModel.getCurrentMonthExpenseSum();
            updateBudgetUI();
        });
        
        // Force initial UI update
        Log.d(TAG, "Forcing initial UI update");
        updateBudgetUI();
    }
    
    /**
     * Update the budget UI with the latest data
     */
    private void updateBudgetUI() {
        Log.d(TAG, "updateBudgetUI called");
        
        // Get the latest budget value
        Double budget = expenseViewModel.getMonthlyBudget().getValue();
        
        // Force a refresh of the expense sum by getting the current month expense sum again
        // This ensures we have the most up-to-date expense data
        Double expenseSum = expenseViewModel.getCurrentMonthExpenseSum().getValue();
        
        Log.d(TAG, "updateBudgetUI - budget: " + budget + ", expenseSum: " + expenseSum);
        
        // Ensure we have valid values to work with
        double validBudget = (budget != null && budget > 0) ? budget : 1000.0;
        double validExpenseSum = (expenseSum != null) ? expenseSum : 0.0;
        
        Log.d(TAG, "updateBudgetUI - using validBudget: " + validBudget + ", validExpenseSum: " + validExpenseSum);
        
        // Always update the UI with the best available values
        updateBudgetProgress(validBudget, validExpenseSum);
        
        // Log the calculation for debugging
        double remaining = validBudget - validExpenseSum;
        Log.d(TAG, "Budget calculation: " + validBudget + " - " + validExpenseSum + " = " + remaining);
    }

    /**
     * Save the budget amount entered by the user
     * Sets the new budget amount directly (not adding to existing budget)
     */
    private void saveBudget() {
        Log.d(TAG, "saveBudget method called");
        String budgetStr = editTextBudget.getText() != null ? editTextBudget.getText().toString() : "";
        Log.d(TAG, "Budget input string: '" + budgetStr + "'");
        
        if (budgetStr.isEmpty()) {
            Log.d(TAG, "Budget string is empty");
            textInputLayoutBudget.setError(getString(R.string.error_empty_budget));
            return;
        }
        
        try {
            double newBudgetAmount = Double.parseDouble(budgetStr);
            Log.d(TAG, "Parsed budget amount: " + newBudgetAmount);
            
            if (newBudgetAmount <= 0) {
                Log.d(TAG, "Budget amount is negative or zero");
                textInputLayoutBudget.setError(getString(R.string.error_negative_budget));
                return;
            }
            
            // Set the new budget amount directly (not adding to existing budget)
            Log.d(TAG, "Setting new budget amount in ViewModel: " + newBudgetAmount);
            expenseViewModel.setMonthlyBudget(newBudgetAmount);
            textInputLayoutBudget.setError(null);
            editTextBudget.setText("");
            
            // The LiveData observer will trigger updateBudgetUI automatically
            // No need to manually call updateBudgetProgress or updateBudgetUI here
            
            Log.d(TAG, "Budget saved successfully: " + newBudgetAmount);
            
            // Show a toast with the saved budget
            String message = "Budget set to " + formatCurrency(newBudgetAmount);
            safeShowToast(message, Toast.LENGTH_SHORT);
            
            // For extra safety, force a UI update after a short delay
            if (getView() != null) {
                getView().postDelayed(() -> {
                    Log.d(TAG, "Performing delayed UI update after budget save");
                    updateBudgetUI();
                }, 100); // Short delay to ensure LiveData has propagated
            }
        } catch (NumberFormatException e) {
            Log.d(TAG, "Number format exception: " + e.getMessage());
            textInputLayoutBudget.setError(getString(R.string.error_invalid_number));
        }
    }

    /**
     * Update the budget progress indicator and remaining amount
     * @param budget The current budget amount
     */
    private void updateBudgetProgress(Double budget) {
        if (budget == null || budget <= 0) {
            Log.d(TAG, "Invalid budget value: " + budget);
            return;
        }
        
        // Get the latest expense sum from view model
        Double expenseSum = expenseViewModel.getCurrentMonthExpenseSum().getValue();
        double spent = expenseSum != null ? expenseSum : 0.0;
        
        Log.d(TAG, "updateBudgetProgress(budget) - budget: " + budget + ", spent: " + spent);
        
        // Calculate remaining for logging
        double remaining = budget - spent;
        Log.d(TAG, "Budget calculation: " + budget + " - " + spent + " = " + remaining);
        
        // Use the overloaded method with both parameters
        updateBudgetProgress(budget, spent);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Refreshing budget UI");
        // Force a refresh of the current month expense sum
        if (expenseViewModel != null) {
            expenseViewModel.getCurrentMonthExpenseSum();
        }
        updateBudgetUI();
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: Refreshing budget UI");
        // Force a refresh of the current month expense sum
        if (expenseViewModel != null) {
            expenseViewModel.getCurrentMonthExpenseSum();
            updateBudgetUI();
        }
    }
    
    /**
     * Update the budget progress indicator and remaining amount with specified budget and expense sum
     * This is the main method that handles all UI updates related to budget display
     * 
     * @param budget The current budget amount
     * @param expenseSum The current expense sum
     */
    private void updateBudgetProgress(Double budget, Double expenseSum) {
        Log.d(TAG, "updateBudgetProgress(budget, expenseSum) called with budget: " + budget + ", expenseSum: " + expenseSum);
        
        if (budget == null || budget <= 0) {
            Log.d(TAG, "Invalid budget value: " + budget);
            return;
        }
        
        double spent = expenseSum != null ? expenseSum : 0.0;
        
        // Calculate remaining amount (can be negative if over budget)
        double remaining = budget - spent;
        
        // Verify budget calculation logic
        verifyBudgetCalculation(budget, spent, remaining);
        Log.d(TAG, "Updating UI - Budget: " + budget + ", Spent: " + spent + ", Remaining: " + remaining);
        
        // Update budget and spending text views first
        String formattedBudget = formatCurrency(budget);
        String formattedSpent = formatCurrency(spent);
        String formattedRemaining = formatCurrency(remaining);
        
        if (safeSetText(textViewCurrentBudget, formattedBudget)) {
            Log.d(TAG, "Updated textViewCurrentBudget with: " + formattedBudget);
        }
        
        if (safeSetText(textViewCurrentSpending, formattedSpent)) {
            Log.d(TAG, "Updated textViewCurrentSpending with: " + formattedSpent);
        }
        
        // Update remaining text with the current value (will show negative if exceeded)
        if (safeSetText(textViewRemaining, formattedRemaining)) {
            Log.d(TAG, "Updated textViewRemaining with: " + formattedRemaining);
        } else {
            Log.d(TAG, "textViewRemaining is null");
        }
        
        // Calculate progress percentage (avoid division by zero)
        int progress = 0;
        if (budget > 0) {
            progress = (int) ((spent / budget) * 100);
            Log.d(TAG, "Progress calculated: " + progress + "%");
        }
        
        // Progress will be capped at 100% in safeSetProgress method
        if (safeSetProgress(progressIndicator, progress)) {
            Log.d(TAG, "Set progress indicator to: " + Math.min(progress, 100) + "%");
        } else {
            Log.d(TAG, "progressIndicator is null");
        }
        
        // Change color based on progress
        int colorResId;
        if (progress >= 100) {
            colorResId = R.color.budget_exceeded;
            Log.d(TAG, "Using budget_exceeded color");
        } else if (progress >= 80) {
            colorResId = R.color.budget_warning;
            Log.d(TAG, "Using budget_warning color");
        } else {
            colorResId = R.color.budget_good;
            Log.d(TAG, "Using budget_good color");
        }
        
        // Apply colors to UI components
        if (getContext() != null) {
            try {
                int color = getResources().getColor(colorResId, null);
                Log.d(TAG, "Color resource resolved");
                
                if (safeSetColor(progressIndicator, color)) {
                    Log.d(TAG, "Applied color to progressIndicator");
                }
                
                if (safeSetColor(textViewRemaining, color)) {
                    Log.d(TAG, "Applied color to textViewRemaining");
                }
            } catch (Exception e) {
                Log.d(TAG, "Error applying colors: " + e.getMessage());
            }
        } else {
            Log.d(TAG, "Context is null");
        }
        
        // Force layout updates to ensure UI reflects current state
        if (safeRequestLayout(progressIndicator)) {
            Log.d(TAG, "Requested layout for progressIndicator");
        }
        
        if (safeRequestLayout(textViewRemaining)) {
            Log.d(TAG, "Requested layout for textViewRemaining");
        }
        
        // Force parent view to redraw
        View rootView = getView();
        if (safeRequestLayout(rootView)) {
            Log.d(TAG, "Invalidated and requested layout for root view");
            
            // Schedule a post-layout update
            rootView.post(() -> {
                Log.d(TAG, "Performing post-layout update");
                rootView.invalidate();
            });
        } else {
            Log.d(TAG, "Root view is null");
        }
    }
}