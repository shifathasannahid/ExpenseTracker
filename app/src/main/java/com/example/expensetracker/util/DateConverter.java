package com.example.expensetracker.util;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Type converter for Room database to convert between Date objects and Long timestamps.
 * This is necessary because Room cannot store complex objects directly.
 */
public class DateConverter {
    
    /**
     * Convert from Date to Long timestamp for database storage
     * @param date Date to convert
     * @return Long timestamp (milliseconds since epoch)
     */
    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }
    
    /**
     * Convert from Long timestamp to Date for application use
     * @param timestamp Long timestamp (milliseconds since epoch)
     * @return Date object
     */
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
}