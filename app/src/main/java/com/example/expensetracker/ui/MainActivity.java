package com.example.expensetracker.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.expensetracker.R;
import com.example.expensetracker.viewmodel.ExpenseViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Main activity for the Expense Tracker app.
 * Hosts fragments for different screens and manages navigation.
 */
public class MainActivity extends AppCompatActivity {

    private ExpenseViewModel expenseViewModel;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up ViewModel
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        // Set up Toolbar
        setSupportActionBar(findViewById(R.id.toolbar));

        // Set up Navigation
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_expenses, R.id.navigation_statistics, R.id.navigation_budget)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Set up Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Set up FAB
        FloatingActionButton fab = findViewById(R.id.fab_add_expense);
        fab.setOnClickListener(view -> {
            // Navigate to add expense fragment
            navController.navigate(R.id.navigation_add_expense);
        });

        // Hide FAB on add/edit expense screen
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_add_expense || 
                destination.getId() == R.id.navigation_edit_expense) {
                fab.hide();
            } else {
                fab.show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}