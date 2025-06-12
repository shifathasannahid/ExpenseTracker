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

        // Observe budget and expenses together to ensure consistent updates
        expenseViewModel.getMonthlyBudget().observe(getViewLifecycleOwner(), budget -> {
            if (textViewCurrentBudget != null) {
                textViewCurrentBudget.setText(currencyFormat.format(budget));
            }
            updateBudgetUI();
        });

        // Observe current month expenses
        expenseViewModel.getCurrentMonthExpenseSum().observe(getViewLifecycleOwner(), sum -> {
            double expenseSum = sum != null ? sum : 0.0;
            if (textViewCurrentSpending != null) {
                textViewCurrentSpending.setText(currencyFormat.format(expenseSum));
            }
            updateBudgetUI();
        });
    }
    
    /**
     * Update all budget UI elements based on current data
     * This ensures consistent updates across all related UI elements
     */
    private void updateBudgetUI() {
        Double budget = expenseViewModel.getMonthlyBudget().getValue();
        Double expenseSum = expenseViewModel.getCurrentMonthExpenseSum().getValue();
        
        if (budget != null && budget > 0) {
            updateBudgetProgress(budget);
        }
    }

    /**
     * Save the budget amount entered by the user
     * Sets the new budget amount directly (not adding to existing budget)
     */
    private void saveBudget() {
        String budgetStr = editTextBudget.getText() != null ? editTextBudget.getText().toString() : "";
        
        if (budgetStr.isEmpty()) {
            textInputLayoutBudget.setError(getString(R.string.error_empty_budget));
            return;
        }
        
        try {
            double newBudgetAmount = Double.parseDouble(budgetStr);
            if (newBudgetAmount <= 0) {
                textInputLayoutBudget.setError(getString(R.string.error_negative_budget));
                return;
            }
            
            // Set the new budget amount directly (not adding to existing budget)
            expenseViewModel.setMonthlyBudget(newBudgetAmount);
            textInputLayoutBudget.setError(null);
            editTextBudget.setText("");
            
            // Update the budget progress with the new budget value
            updateBudgetProgress(newBudgetAmount);
        } catch (NumberFormatException e) {
            textInputLayoutBudget.setError(getString(R.string.error_invalid_number));
        }
    }

    /**
     * Update the budget progress indicator and remaining amount
     * @param budget The current budget amount
     */
    private void updateBudgetProgress(Double budget) {
        if (budget == null || budget <= 0) return;
        
        // Get the latest expense sum from view model
        Double expenseSum = expenseViewModel.getCurrentMonthExpenseSum().getValue();
        double spent = expenseSum != null ? expenseSum : 0.0;
        
        // Calculate remaining amount (can be negative if exceeded)
        double remaining = budget - spent;
        
        // Update remaining text with the current value (will show negative if exceeded)
        if (textViewRemaining != null) {
            textViewRemaining.setText(currencyFormat.format(remaining));
        }
        
        // Calculate progress percentage (avoid division by zero)
        int progress = 0;
        if (budget > 0) {
            progress = (int) ((spent / budget) * 100);
        }
        
        // Ensure progress doesn't exceed 100% for visual purposes only
        if (progressIndicator != null) {
            progressIndicator.setProgress(Math.min(progress, 100));
        }
        
        // Change color based on progress
        int colorResId;
        if (progress >= 100) {
            colorResId = R.color.budget_exceeded;
        } else if (progress >= 80) {
            colorResId = R.color.budget_warning;
        } else {
            colorResId = R.color.budget_good;
        }
        
        // Apply colors to UI components
        if (getContext() != null) {
            int color = getResources().getColor(colorResId, null);
            
            if (progressIndicator != null) {
                progressIndicator.setIndicatorColor(color);
                progressIndicator.invalidate();
            }
            
            if (textViewRemaining != null) {
                textViewRemaining.setTextColor(color);
                textViewRemaining.invalidate();
            }
        }
        
        // Force layout updates to ensure UI reflects current state
        if (progressIndicator != null) {
            progressIndicator.requestLayout();
        }
        
        if (textViewRemaining != null) {
            textViewRemaining.requestLayout();
        }
        
        // Force parent view to redraw
        View rootView = getView();
        if (rootView != null) {
            rootView.invalidate();
            rootView.requestLayout();
        }
    }
}