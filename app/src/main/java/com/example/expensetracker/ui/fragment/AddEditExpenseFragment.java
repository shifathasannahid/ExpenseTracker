package com.example.expensetracker.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.expensetracker.viewmodel.ExpenseViewModel;

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
        // Since we don't have the actual layout file yet, we'll use a simple layout from an existing fragment
        // In a real app, you would create the proper fragment_add_edit_expense.xml layout
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);
        
        // This is a temporary solution - in a real app, you would find the proper views from your layout
        // editTextAmount = view.findViewById(R.id.edit_text_amount);
        // editTextDescription = view.findViewById(R.id.edit_text_description);
        // spinnerCategory = view.findViewById(R.id.spinner_category);
        
        // If editing an existing expense, load its data
        if (expenseId != -1L) {
            // Load expense data from ViewModel
            // expenseViewModel.getExpenseById(expenseId).observe(getViewLifecycleOwner(), expense -> {
            //     if (expense != null) {
            //         editTextAmount.setText(String.valueOf(expense.getAmount()));
            //         editTextDescription.setText(expense.getDescription());
            //         // Set spinner selection based on category
            //     }
            // });
        }
        
        // Set up save button
        Button saveButton = view.findViewById(R.id.button_save);
        if (saveButton != null) {
            saveButton.setOnClickListener(v -> saveExpense());
        }
        
        return view;
    }
    
    private void saveExpense() {
        // This is a placeholder implementation
        // In a real app, you would validate inputs and save the expense
        
        // Show a toast message
        Toast.makeText(requireContext(), "Expense saved", Toast.LENGTH_SHORT).show();
        
        // Navigate back
        Navigation.findNavController(requireView()).navigateUp();
    }
}