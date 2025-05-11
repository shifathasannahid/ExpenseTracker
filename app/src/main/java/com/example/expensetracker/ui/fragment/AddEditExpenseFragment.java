package com.example.expensetracker.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.expensetracker.R;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.data.model.Category;
import com.example.expensetracker.viewmodel.ExpenseViewModel;

import java.util.Date;

/**
 * Fragment for adding or editing an expense.
 */
public class AddEditExpenseFragment extends Fragment {

    private ExpenseViewModel expenseViewModel;
    private EditText editTextAmount;
    private EditText editTextDescription;
    private Spinner spinnerCategory;
    private long expenseId = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        expenseViewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
        
        // Get expense ID from arguments if editing an existing expense
        if (getArguments() != null) {
            expenseId = AddEditExpenseFragmentArgs.fromBundle(getArguments()).getExpenseId();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the correct layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_edit_expense, container, false);
        
        // Initialize views from the layout
        editTextAmount = view.findViewById(R.id.edit_text_amount);
        editTextDescription = view.findViewById(R.id.edit_text_description);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        
        // Set up category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Category.getAllDisplayNames());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        
        // If editing an existing expense, load its data
        if (expenseId != -1L) {
            // Load expense data from ViewModel
            expenseViewModel.getExpenseById(expenseId).observe(getViewLifecycleOwner(), expense -> {
                if (expense != null) {
                    editTextAmount.setText(String.valueOf(expense.getAmount()));
                    editTextDescription.setText(expense.getNotes());
                    
                    // Set spinner selection based on category
                    String categoryName = expense.getCategory();
                    for (int i = 0; i < categoryAdapter.getCount(); i++) {
                        if (categoryAdapter.getItem(i).equals(categoryName)) {
                            spinnerCategory.setSelection(i);
                            break;
                        }
                    }
                }
            });
        }
        
        // Set up save button
        Button saveButton = view.findViewById(R.id.button_save);
        if (saveButton != null) {
            saveButton.setOnClickListener(v -> saveExpense());
        }
        
        return view;
    }
    
    private void saveExpense() {
        // Validate inputs
        String amountStr = editTextAmount.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String categoryStr = spinnerCategory.getSelectedItem().toString();
        
        if (amountStr.isEmpty()) {
            editTextAmount.setError("Please enter an amount");
            return;
        }
        
        try {
            double amount = Double.parseDouble(amountStr);
            
            // Create or update expense
            if (expenseId == -1L) {
                // Create new expense
                Expense newExpense = new Expense(amount, categoryStr, new Date(), description);
                expenseViewModel.insert(newExpense);
                Toast.makeText(requireContext(), "Expense added", Toast.LENGTH_SHORT).show();
            } else {
                // Update existing expense
                expenseViewModel.getExpenseById(expenseId).observe(getViewLifecycleOwner(), expense -> {
                    if (expense != null) {
                        expense.setAmount(amount);
                        expense.setCategory(categoryStr);
                        expense.setNotes(description);
                        expenseViewModel.update(expense);
                    }
                });
                Toast.makeText(requireContext(), "Expense updated", Toast.LENGTH_SHORT).show();
            }
            
            // Navigate back
            Navigation.findNavController(requireView()).navigateUp();
            
        } catch (NumberFormatException e) {
            editTextAmount.setError("Please enter a valid amount");
        }
    }
}