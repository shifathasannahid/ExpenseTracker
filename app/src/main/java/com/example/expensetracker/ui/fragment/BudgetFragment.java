package com.example.expensetracker.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Fragment for setting and tracking monthly budget.
 * Allows users to set a budget amount and displays progress towards that budget.
 */
public class BudgetFragment extends Fragment {

    private ExpenseViewModel expenseViewModel;
    private TextInputLayout textInputLayoutBudget;
    private TextInputEditText editTextBudget;
    private Button buttonSaveBudget;
    private TextView textViewCurrentBudget;
    private TextView textViewCurrentSpending;
    private TextView textViewRemaining;
    private LinearProgressIndicator progressIndicator;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("bn", "BD"));

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

        System.out.println("DEBUG: Setting up LiveData observers");
        
        // Observe budget and expenses together to ensure consistent updates
        expenseViewModel.getMonthlyBudget().observe(getViewLifecycleOwner(), budget -> {
            System.out.println("DEBUG: Budget LiveData updated: " + budget);
            if (textViewCurrentBudget != null) {
                textViewCurrentBudget.setText(currencyFormat.format(budget));
                System.out.println("DEBUG: Updated textViewCurrentBudget");
            } else {
                System.out.println("DEBUG: textViewCurrentBudget is null");
            }
            updateBudgetUI();
        });

        // Observe current month expenses
        expenseViewModel.getCurrentMonthExpenseSum().observe(getViewLifecycleOwner(), sum -> {
            double expenseSum = sum != null ? sum : 0.0;
            System.out.println("DEBUG: Expense sum LiveData updated: " + expenseSum);
            if (textViewCurrentSpending != null) {
                textViewCurrentSpending.setText(currencyFormat.format(expenseSum));
                System.out.println("DEBUG: Updated textViewCurrentSpending");
            } else {
                System.out.println("DEBUG: textViewCurrentSpending is null");
            }
            updateBudgetUI();
        });
        
        // Force initial UI update
        System.out.println("DEBUG: Forcing initial UI update");
        updateBudgetUI();
    }
    
    /**
     * Update the budget UI with the latest data
     */
    private void updateBudgetUI() {
        System.out.println("DEBUG: updateBudgetUI called");
        
        Double budget = expenseViewModel.getMonthlyBudget().getValue();
        Double expenseSum = expenseViewModel.getCurrentMonthExpenseSum().getValue();
        
        System.out.println("DEBUG: updateBudgetUI - budget: " + budget + ", expenseSum: " + expenseSum);
        
        if (budget != null && expenseSum != null) {
            System.out.println("DEBUG: updateBudgetUI - calling updateBudgetProgress");
            updateBudgetProgress(budget, expenseSum);
        } else {
            System.out.println("DEBUG: updateBudgetUI - NOT calling updateBudgetProgress because " + 
                              (budget == null ? "budget is null" : "") + 
                              (expenseSum == null ? "expenseSum is null" : ""));
            
            // Force update with default values if data is missing
            if (budget == null && expenseSum != null) {
                System.out.println("DEBUG: updateBudgetUI - forcing update with default budget");
                updateBudgetProgress(1000.0, expenseSum); // Use default budget
            } else if (budget != null && expenseSum == null) {
                System.out.println("DEBUG: updateBudgetUI - forcing update with zero expenses");
                updateBudgetProgress(budget, 0.0); // Use zero expenses
            } else if (budget == null && expenseSum == null) {
                System.out.println("DEBUG: updateBudgetUI - forcing update with default values");
                updateBudgetProgress(1000.0, 0.0); // Use default values
            }
        }
    }

    /**
     * Save the budget amount entered by the user
     * Sets the new budget amount directly (not adding to existing budget)
     */
    private void saveBudget() {
        System.out.println("DEBUG: saveBudget method called");
        String budgetStr = editTextBudget.getText() != null ? editTextBudget.getText().toString() : "";
        System.out.println("DEBUG: Budget input string: '" + budgetStr + "'");
        
        if (budgetStr.isEmpty()) {
            System.out.println("DEBUG: Budget string is empty");
            textInputLayoutBudget.setError(getString(R.string.error_empty_budget));
            return;
        }
        
        try {
            double newBudgetAmount = Double.parseDouble(budgetStr);
            System.out.println("DEBUG: Parsed budget amount: " + newBudgetAmount);
            
            if (newBudgetAmount <= 0) {
                System.out.println("DEBUG: Budget amount is negative or zero");
                textInputLayoutBudget.setError(getString(R.string.error_negative_budget));
                return;
            }
            
            // Set the new budget amount directly (not adding to existing budget)
            System.out.println("DEBUG: Setting new budget amount in ViewModel: " + newBudgetAmount);
            expenseViewModel.setMonthlyBudget(newBudgetAmount);
            textInputLayoutBudget.setError(null);
            editTextBudget.setText("");
            
            // Update the budget progress with the new budget value
            System.out.println("DEBUG: Calling updateBudgetProgress with new budget");
            updateBudgetProgress(newBudgetAmount);
            
            // Force UI refresh
            System.out.println("DEBUG: Forcing UI refresh after budget save");
            if (getView() != null) {
                getView().post(() -> {
                    updateBudgetUI();
                    System.out.println("DEBUG: Post-save UI update complete");
                });
            }
        } catch (NumberFormatException e) {
            System.out.println("DEBUG: Number format exception: " + e.getMessage());
            textInputLayoutBudget.setError(getString(R.string.error_invalid_number));
        }
    }

    /**
     * Update the budget progress indicator and remaining amount
     * @param budget The current budget amount
     */
    private void updateBudgetProgress(Double budget) {
        if (budget == null || budget <= 0) {
            System.out.println("DEBUG: Invalid budget value: " + budget);
            return;
        }
        
        // Get the latest expense sum from view model
        Double expenseSum = expenseViewModel.getCurrentMonthExpenseSum().getValue();
        double spent = expenseSum != null ? expenseSum : 0.0;
        
        // Use the overloaded method with both parameters
        updateBudgetProgress(budget, spent);
    }
    
    /**
     * Update the budget progress indicator and remaining amount with specified expense sum
     * @param budget The current budget amount
     * @param expenseSum The current expense sum
     */
    private void updateBudgetProgress(Double budget, Double expenseSum) {
        System.out.println("DEBUG: updateBudgetProgress(budget, expenseSum) called with budget: " + budget + ", expenseSum: " + expenseSum);
        
        if (budget == null || budget <= 0) {
            System.out.println("DEBUG: Invalid budget value: " + budget);
            return;
        }
        
        double spent = expenseSum != null ? expenseSum : 0.0;
        
        // Calculate remaining amount (can be negative if exceeded)
        double remaining = budget - spent;
        
        System.out.println("DEBUG: Updating UI - Budget: " + budget + ", Spent: " + spent + ", Remaining: " + remaining);
        
        // Update remaining text with the current value (will show negative if exceeded)
        if (textViewRemaining != null) {
            textViewRemaining.setText(currencyFormat.format(remaining));
            System.out.println("DEBUG: Updated textViewRemaining with: " + currencyFormat.format(remaining));
        } else {
            System.out.println("DEBUG: textViewRemaining is null");
        }
        
        // Calculate progress percentage (avoid division by zero)
        int progress = 0;
        if (budget > 0) {
            progress = (int) ((spent / budget) * 100);
            System.out.println("DEBUG: Progress calculated: " + progress + "%");
        }
        
        // Ensure progress doesn't exceed 100% for visual purposes only
        if (progressIndicator != null) {
            int cappedProgress = Math.min(progress, 100);
            progressIndicator.setProgress(cappedProgress);
            System.out.println("DEBUG: Set progress indicator to " + cappedProgress + "%");
        } else {
            System.out.println("DEBUG: progressIndicator is null");
        }
        
        // Change color based on progress
        int colorResId;
        if (progress >= 100) {
            colorResId = R.color.budget_exceeded;
            System.out.println("DEBUG: Using budget_exceeded color");
        } else if (progress >= 80) {
            colorResId = R.color.budget_warning;
            System.out.println("DEBUG: Using budget_warning color");
        } else {
            colorResId = R.color.budget_good;
            System.out.println("DEBUG: Using budget_good color");
        }
        
        // Apply colors to UI components
        if (getContext() != null) {
            try {
                int color = getResources().getColor(colorResId, null);
                System.out.println("DEBUG: Color resource resolved");
                
                if (progressIndicator != null) {
                    progressIndicator.setIndicatorColor(color);
                    progressIndicator.invalidate();
                    System.out.println("DEBUG: Applied color to progressIndicator");
                }
                
                if (textViewRemaining != null) {
                    textViewRemaining.setTextColor(color);
                    textViewRemaining.invalidate();
                    System.out.println("DEBUG: Applied color to textViewRemaining");
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Error applying colors: " + e.getMessage());
            }
        } else {
            System.out.println("DEBUG: Context is null");
        }
        
        // Force layout updates to ensure UI reflects current state
        if (progressIndicator != null) {
            progressIndicator.requestLayout();
            System.out.println("DEBUG: Requested layout for progressIndicator");
        }
        
        if (textViewRemaining != null) {
            textViewRemaining.requestLayout();
            System.out.println("DEBUG: Requested layout for textViewRemaining");
        }
        
        // Force parent view to redraw
        View rootView = getView();
        if (rootView != null) {
            rootView.invalidate();
            rootView.requestLayout();
            System.out.println("DEBUG: Invalidated and requested layout for root view");
        } else {
            System.out.println("DEBUG: Root view is null");
        }
        
        // Force immediate layout pass
        if (rootView != null) {
            rootView.post(() -> {
                System.out.println("DEBUG: Performing post-layout update");
                rootView.invalidate();
            });
        }
    }
}