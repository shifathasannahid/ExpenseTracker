package com.example.expensetracker.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.ui.adapter.ExpenseAdapter;
import com.example.expensetracker.util.CsvExportUtil;
import com.example.expensetracker.viewmodel.ExpenseViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying the list of expenses.
 * Uses RecyclerView with ExpenseAdapter to show expenses.
 */
public class ExpensesFragment extends Fragment implements ExpenseAdapter.OnItemClickListener {

    private ExpenseViewModel expenseViewModel;
    private ExpenseAdapter adapter;
    private List<Expense> currentExpenses = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Enable options menu
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_expenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);

        // Set up adapter
        adapter = new ExpenseAdapter(requireContext(), this);
        recyclerView.setAdapter(adapter);

        // Set up ViewModel
        expenseViewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
        
        // Observe expenses
        expenseViewModel.getAllExpenses().observe(getViewLifecycleOwner(), expenses -> {
            currentExpenses = expenses;
            adapter.submitList(expenses);
            
            // Show empty state if no expenses
            View emptyView = view.findViewById(R.id.empty_view);
            if (expenses.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onItemClick(Expense expense) {
        // Navigate to edit expense screen
        navigateToEditExpense(expense);
    }
    
    @Override
    public void onEditClick(Expense expense) {
        // Navigate to edit expense screen
        navigateToEditExpense(expense);
    }
    
    @Override
    public void onDeleteClick(Expense expense) {
        // Show confirmation dialog before deleting
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the expense
                    expenseViewModel.delete(expense);
                    Toast.makeText(requireContext(), "Expense deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Helper method to navigate to edit expense screen
     */
    private void navigateToEditExpense(Expense expense) {
        Bundle args = new Bundle();
        args.putLong("expenseId", expense.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_expenses_to_edit_expense, args);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.expenses_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export_csv) {
            exportToCsv();
            return true;
        } else if (id == R.id.action_categories) {
            // Navigate to categories fragment
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_navigation_expenses_to_categories);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Export expenses to CSV file
     */
    private void exportToCsv() {
        if (currentExpenses.isEmpty()) {
            Toast.makeText(requireContext(), "No expenses to export", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Export Expenses")
                .setMessage("Do you want to export all expenses to a CSV file?")
                .setPositiveButton("Export", (dialog, which) -> {
                    String filePath = CsvExportUtil.exportToDownloads(requireContext(), currentExpenses);
                    if (filePath != null) {
                        Toast.makeText(requireContext(), 
                                "Exported to: " + filePath, 
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), 
                                "Failed to export", 
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}