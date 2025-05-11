package com.example.expensetracker.data.model;

import androidx.annotation.NonNull;

/**
 * Enum representing expense categories.
 */
public enum Category {
    FOOD("Food"),
    TRANSPORTATION("Transportation"),
    HOUSING("Housing"),
    ENTERTAINMENT("Entertainment"),
    SHOPPING("Shopping"),
    UTILITIES("Utilities"),
    HEALTHCARE("Healthcare"),
    EDUCATION("Education"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    @NonNull
    public static String[] getAllDisplayNames() {
        Category[] categories = Category.values();
        String[] names = new String[categories.length];
        
        for (int i = 0; i < categories.length; i++) {
            names[i] = categories[i].getDisplayName();
        }
        
        return names;
    }
    
    @NonNull
    public static Category fromDisplayName(String displayName) {
        for (Category category : Category.values()) {
            if (category.getDisplayName().equals(displayName)) {
                return category;
            }
        }
        return OTHER; // Default to OTHER if no match found
    }
}