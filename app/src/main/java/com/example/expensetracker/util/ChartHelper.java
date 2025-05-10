package com.example.expensetracker.util;

import android.content.Context;
import android.graphics.Color;

import com.example.expensetracker.data.dao.ExpenseDao.CategorySum;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for setting up and configuring charts using MPAndroidChart library.
 * Provides methods for creating pie charts and bar charts for expense data visualization.
 */
public class ChartHelper {

    /**
     * Set up a pie chart for category distribution
     * @param pieChart The PieChart view to configure
     * @param categorySums List of category sums to display
     * @param context Application context
     */
    public static void setupPieChart(PieChart pieChart, List<CategorySum> categorySums, Context context) {
        // Configure pie chart appearance
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        
        // Configure legend
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        
        // Create entries from data
        List<PieEntry> entries = new ArrayList<>();
        for (CategorySum categorySum : categorySums) {
            entries.add(new PieEntry((float) categorySum.total, categorySum.category));
        }
        
        // Create dataset
        PieDataSet dataSet = new PieDataSet(entries, "Categories");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        
        // Set colors
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS) {
            colors.add(c);
        }
        for (int c : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(c);
        }
        dataSet.setColors(colors);
        
        // Configure data
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        
        pieChart.setData(data);
        pieChart.highlightValues(null);
        pieChart.invalidate();
    }
    
    /**
     * Set up a bar chart for category comparison
     * @param barChart The BarChart view to configure
     * @param categorySums List of category sums to display
     * @param context Application context
     */
    public static void setupBarChart(BarChart barChart, List<CategorySum> categorySums, Context context) {
        // Configure bar chart appearance
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        
        // Configure X axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        
        // Create entries and labels from data
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        
        for (int i = 0; i < categorySums.size(); i++) {
            CategorySum categorySum = categorySums.get(i);
            entries.add(new BarEntry(i, (float) categorySum.total));
            labels.add(categorySum.category);
        }
        
        // Set X axis labels
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());
        xAxis.setLabelRotationAngle(45f);
        
        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Expenses by Category");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        
        // Configure data
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);
        data.setValueTextSize(10f);
        
        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.invalidate();
    }
}