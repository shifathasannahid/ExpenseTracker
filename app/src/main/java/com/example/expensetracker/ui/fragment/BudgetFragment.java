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

        // Observe budget
        expenseViewModel.getMonthlyBudget().observe(getViewLifecycleOwner(), budget -> {
            textViewCurrentBudget.setText(currencyFormat.format(budget));
            updateBudgetProgress(budget);
        });

        // Observe current month expenses
        expenseViewModel.getCurrentMonthExpenseSum().observe(getViewLifecycleOwner(), sum -> {
            double expenseSum = sum != null ? sum : 0.0;
            textViewCurrentSpending.setText(currencyFormat.format(expenseSum));
            updateBudgetProgress(expenseViewModel.getMonthlyBudget().getValue());
        });
    }

    /**
     * Save the budget amount entered by the user
     */
    private void saveBudget() {
        String budgetStr = editTextBudget.getText() != null ? editTextBudget.getText().toString() : "";
        
        if (budgetStr.isEmpty()) {
            textInputLayoutBudget.setError(getString(R.string.error_empty_budget));
            return;
        }
        
        try {
            double budget = Double.parseDouble(budgetStr);
            if (budget <= 0) {
                textInputLayoutBudget.setError(getString(R.string.error_negative_budget));
                return;
            }
            
            expenseViewModel.setMonthlyBudget(budget);
            textInputLayoutBudget.setError(null);
            editTextBudget.setText("");
        } catch (NumberFormatException e) {
            textInputLayoutBudget.setError(getString(R.string.error_invalid_number));
        }
    }

    /**
     * Update the budget progress indicator and remaining amount
     * @param budget The current budget amount
     */
    private void updateBudgetProgress(Double budget) {
        if (budget == null) return;
        
        Double expenseSum = expenseViewModel.getCurrentMonthExpenseSum().getValue();
        double spent = expenseSum != null ? expenseSum : 0.0;
        double remaining = budget - spent;
        
        // Update remaining text
        textViewRemaining.setText(currencyFormat.format(remaining));
        
        // Update progress indicator
        int progress = budget > 0 ? (int) ((spent / budget) * 100) : 0;
        progressIndicator.setProgress(Math.min(progress, 100));
        
        // Change color based on progress
        if (progress >= 100) {
            progressIndicator.setIndicatorColor(getResources().getColor(R.color.budget_exceeded, null));
            textViewRemaining.setTextColor(getResources().getColor(R.color.budget_exceeded, null));
        } else if (progress >= 80) {
            progressIndicator.setIndicatorColor(getResources().getColor(R.color.budget_warning, null));
            textViewRemaining.setTextColor(getResources().getColor(R.color.budget_warning, null));
        } else {
            progressIndicator.setIndicatorColor(getResources().getColor(R.color.budget_good, null));
            textViewRemaining.setTextColor(getResources().getColor(R.color.budget_good, null));
        }
    }
}