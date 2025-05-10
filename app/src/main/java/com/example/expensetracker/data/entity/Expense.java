package com.example.expensetracker.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;

import java.util.Date;

/**
 * Entity class representing an expense entry in the database.
 * Uses Room annotations to define table structure.
 */
@Entity(tableName = "expenses")
public class Expense {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "amount")
    private double amount;
    
    @ColumnInfo(name = "category")
    @NonNull
    private String category;
    
    @ColumnInfo(name = "date")
    @NonNull
    private Date date;
    
    @ColumnInfo(name = "notes")
    private String notes;
    
    /**
     * Constructor for creating a new expense
     */
    public Expense(double amount, @NonNull String category, @NonNull Date date, String notes) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.notes = notes;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    @NonNull
    public String getCategory() {
        return category;
    }
    
    public void setCategory(@NonNull String category) {
        this.category = category;
    }
    
    @NonNull
    public Date getDate() {
        return date;
    }
    
    public void setDate(@NonNull Date date) {
        this.date = date;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}