package com.example.expensetracker.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.expensetracker.R;
import com.example.expensetracker.data.dao.ExpenseDao.CategorySum;
import com.example.expensetracker.util.ChartHelper;
import com.example.expensetracker.viewmodel.ExpenseViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for displaying expense statistics using charts.
 * Uses MPAndroidChart library to visualize expense data.
 */
public class StatisticsFragment extends Fragment {

    private ExpenseViewModel expenseViewModel;
    private PieChart pieChart;
    private BarChart barChart;
    private TextView textViewTotalExpenses;
    private Spinner spinnerMonth;
    private Spinner spinnerYear;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("bn", "BD"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        pieChart = view.findViewById(R.id.pie_chart);
        barChart = view.findViewById(R.id.bar_chart);
        textViewTotalExpenses = view.findViewById(R.id.text_view_total_expenses);
        spinnerMonth = view.findViewById(R.id.spinner_month);
        spinnerYear = view.findViewById(R.id.spinner_year);

        // Set up ViewModel
        expenseViewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        // Set up month spinner
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.months_array,
                android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Set up year spinner
        List<String> years = getYearsList();
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Set default selections to current month and year
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        spinnerMonth.setSelection(currentMonth);
        int yearPosition = years.indexOf(String.valueOf(currentYear));
        if (yearPosition >= 0) {
            spinnerYear.setSelection(yearPosition);
        }

        // Set up spinners listeners
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCharts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCharts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Initial update
        updateCharts();
    }

    /**
     * Update charts based on selected month and year
     */
    private void updateCharts() {
        System.out.println("DEBUG: StatisticsFragment.updateCharts called");
        
        int selectedMonth = spinnerMonth.getSelectedItemPosition() + 1; // +1 because Calendar months are 0-based
        int selectedYear = Integer.parseInt(spinnerYear.getSelectedItem().toString());
        
        System.out.println("DEBUG: Selected month: " + selectedMonth + ", Selected year: " + selectedYear);

        // Update ViewModel with selected month and year
        expenseViewModel.setCurrentMonthAndYear(selectedYear, selectedMonth);
        System.out.println("DEBUG: Updated ViewModel with selected month and year");

        // Observe monthly expense sum
        System.out.println("DEBUG: Setting up expense sum observer");
        expenseViewModel.getCurrentMonthExpenseSum().observe(getViewLifecycleOwner(), sum -> {
            System.out.println("DEBUG: Expense sum updated: " + sum);
            if (sum != null) {
                textViewTotalExpenses.setText(currencyFormat.format(sum));
                System.out.println("DEBUG: Updated total expenses text: " + currencyFormat.format(sum));
            } else {
                textViewTotalExpenses.setText(currencyFormat.format(0));
                System.out.println("DEBUG: Updated total expenses text with zero");
            }
        });

        // Observe category sums for charts
        System.out.println("DEBUG: Setting up category sums observer");
        expenseViewModel.getCurrentMonthCategorySums().observe(getViewLifecycleOwner(), categorySums -> {
            if (categorySums != null) {
                System.out.println("DEBUG: Category sums updated, count: " + categorySums.size());
                
                if (!categorySums.isEmpty()) {
                    System.out.println("DEBUG: Category sums not empty, updating charts");
                    // Show charts
                    pieChart.setVisibility(View.VISIBLE);
                    barChart.setVisibility(View.VISIBLE);
                    requireView().findViewById(R.id.text_view_no_data).setVisibility(View.GONE);

                    // Update charts
                    System.out.println("DEBUG: Setting up pie chart");
                    ChartHelper.setupPieChart(pieChart, categorySums, requireContext());
                    
                    System.out.println("DEBUG: Setting up bar chart");
                    ChartHelper.setupBarChart(barChart, categorySums, requireContext());
                    
                    System.out.println("DEBUG: Charts updated successfully");
                } else {
                    System.out.println("DEBUG: Category sums empty, showing no data message");
                    // Show no data message
                    pieChart.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    requireView().findViewById(R.id.text_view_no_data).setVisibility(View.VISIBLE);
                }
            } else {
                System.out.println("DEBUG: Category sums null, showing no data message");
                // Show no data message
                pieChart.setVisibility(View.GONE);
                barChart.setVisibility(View.GONE);
                requireView().findViewById(R.id.text_view_no_data).setVisibility(View.VISIBLE);
            }
        });
        
        System.out.println("DEBUG: updateCharts setup complete");
    }

    /**
     * Generate a list of years for the spinner
     * @return List of years from 2020 to current year
     */
    private List<String> getYearsList() {
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int year = 2020; year <= currentYear + 1; year++) {
            years.add(String.valueOf(year));
        }
        return years;
    }
}