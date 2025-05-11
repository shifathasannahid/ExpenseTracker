package com.example.expensetracker.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expensetracker.R;
import com.example.expensetracker.data.model.Category;

/**
 * Fragment for displaying and managing expense categories.
 */
public class CategoriesFragment extends Fragment {

    private ListView listViewCategories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        
        // Set title in action bar
        requireActivity().setTitle(R.string.categories);
        
        // Initialize views
        listViewCategories = view.findViewById(R.id.list_view_categories);
        
        // Set up categories list
        setupCategoriesList();
        
        return view;
    }
    
    private void setupCategoriesList() {
        // Get all category display names
        String[] categories = Category.getAllDisplayNames();
        
        // Create adapter for the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                categories);
        
        // Set adapter to ListView
        listViewCategories.setAdapter(adapter);
    }
}