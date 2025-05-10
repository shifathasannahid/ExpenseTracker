package com.example.expensetracker.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.documentfile.provider.DocumentFile;

import com.example.expensetracker.data.entity.Expense;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for exporting expense data to CSV format.
 * Provides methods to export expenses to external storage or a user-selected location.
 */
public class CsvExportUtil {

    private static final String CSV_HEADER = "ID,Amount,Category,Date,Notes\n";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Export expenses to a CSV file in the Downloads directory
     * @param context Application context
     * @param expenses List of expenses to export
     * @return File path if successful, null otherwise
     */
    public static String exportToDownloads(Context context, List<Expense> expenses) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            
            String fileName = "expenses_" + System.currentTimeMillis() + ".csv";
            File file = new File(downloadsDir, fileName);
            
            FileOutputStream fos = new FileOutputStream(file);
            writeExpensesToCsv(fos, expenses);
            
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Export expenses to a user-selected location using Storage Access Framework
     * @param context Application context
     * @param expenses List of expenses to export
     * @param uri URI of the location selected by the user
     * @return true if successful, false otherwise
     */
    public static boolean exportToUri(Context context, List<Expense> expenses, Uri uri) {
        try {
            OutputStream os = context.getContentResolver().openOutputStream(uri);
            if (os != null) {
                writeExpensesToCsv(os, expenses);
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Create a new CSV file at a user-selected directory
     * @param context Application context
     * @param expenses List of expenses to export
     * @param directoryUri URI of the directory selected by the user
     * @return URI of the created file if successful, null otherwise
     */
    public static Uri createCsvInDirectory(Context context, List<Expense> expenses, Uri directoryUri) {
        try {
            DocumentFile pickedDir = DocumentFile.fromTreeUri(context, directoryUri);
            if (pickedDir == null) return null;
            
            String fileName = "expenses_" + System.currentTimeMillis() + ".csv";
            DocumentFile newFile = pickedDir.createFile("text/csv", fileName);
            if (newFile == null) return null;
            
            OutputStream os = context.getContentResolver().openOutputStream(newFile.getUri());
            if (os != null) {
                writeExpensesToCsv(os, expenses);
                return newFile.getUri();
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Write expenses to a CSV file
     * @param os OutputStream to write to
     * @param expenses List of expenses to export
     * @throws IOException If an I/O error occurs
     */
    private static void writeExpensesToCsv(OutputStream os, List<Expense> expenses) throws IOException {
        // Write header
        os.write(CSV_HEADER.getBytes());
        
        // Write data rows
        for (Expense expense : expenses) {
            StringBuilder sb = new StringBuilder();
            sb.append(expense.getId()).append(',');
            sb.append(expense.getAmount()).append(',');
            sb.append(escapeSpecialCharacters(expense.getCategory())).append(',');
            sb.append(DATE_FORMAT.format(expense.getDate())).append(',');
            sb.append(escapeSpecialCharacters(expense.getNotes())).append('\n');
            
            os.write(sb.toString().getBytes());
        }
        
        os.flush();
        os.close();
    }
    
    /**
     * Escape special characters in CSV fields
     * @param data The string to escape
     * @return Escaped string
     */
    private static String escapeSpecialCharacters(String data) {
        if (data == null) return "";
        
        String escapedData = data.replaceAll("\"", "\"\"");
        if (escapedData.contains(",") || escapedData.contains("\"") || 
            escapedData.contains("\n") || escapedData.contains("\r")) {
            return "\""+escapedData+"\"";
        }
        return escapedData;
    }
}