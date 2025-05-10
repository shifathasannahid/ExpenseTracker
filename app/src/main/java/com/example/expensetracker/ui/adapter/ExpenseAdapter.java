package com.example.expensetracker.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.data.entity.Expense;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Adapter for displaying expenses in a RecyclerView using Material Design 3 components.
 * Uses ListAdapter for efficient updates with DiffUtil.
 */
public class ExpenseAdapter extends ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder> {
    
    private final Context context;
    private final OnItemClickListener listener;
    private final SimpleDateFormat dateFormat;
    private final NumberFormat currencyFormat;
    
    /**
     * Interface for handling item clicks
     */
    public interface OnItemClickListener {
        void onItemClick(Expense expense);
    }
    
    /**
     * Constructor
     * @param context Context for inflating views
     * @param listener Click listener for items
     */
    public ExpenseAdapter(Context context, OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.currencyFormat = NumberFormat.getCurrencyInstance();
    }
    
    /**
     * DiffUtil callback for efficient updates
     */
    private static final DiffUtil.ItemCallback<Expense> DIFF_CALLBACK = new DiffUtil.ItemCallback<Expense>() {
        @Override
        public boolean areItemsTheSame(@NonNull Expense oldItem, @NonNull Expense newItem) {
            return oldItem.getId() == newItem.getId();
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull Expense oldItem, @NonNull Expense newItem) {
            return oldItem.getAmount() == newItem.getAmount() &&
                   oldItem.getCategory().equals(newItem.getCategory()) &&
                   oldItem.getDate().equals(newItem.getDate()) &&
                   (oldItem.getNotes() == null ? newItem.getNotes() == null : 
                    oldItem.getNotes().equals(newItem.getNotes()));
        }
    };
    
    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense currentExpense = getItem(position);
        
        holder.textViewAmount.setText(currencyFormat.format(currentExpense.getAmount()));
        holder.textViewCategory.setText(currentExpense.getCategory());
        holder.textViewDate.setText(dateFormat.format(currentExpense.getDate()));
        
        // Set notes if available, otherwise hide the notes TextView
        if (currentExpense.getNotes() != null && !currentExpense.getNotes().isEmpty()) {
            holder.textViewNotes.setText(currentExpense.getNotes());
            holder.textViewNotes.setVisibility(View.VISIBLE);
        } else {
            holder.textViewNotes.setVisibility(View.GONE);
        }
        
        // Set card color based on category (could be customized per category)
        // This would be implemented with a CategoryColorManager in a real app
    }
    
    /**
     * ViewHolder for expense items
     */
    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewAmount;
        private final TextView textViewCategory;
        private final TextView textViewDate;
        private final TextView textViewNotes;
        private final MaterialCardView cardView;
        
        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAmount = itemView.findViewById(R.id.text_view_amount);
            textViewCategory = itemView.findViewById(R.id.text_view_category);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewNotes = itemView.findViewById(R.id.text_view_notes);
            cardView = itemView.findViewById(R.id.card_view_expense);
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }
    }
}