package com.example.grocerylist;

import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtil {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getToday() {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = simpleDateFormat.format(now.getTime());
        return today;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getTenDaysInFuture() {
        Calendar tenDaysInFuture = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        tenDaysInFuture.add(Calendar.DAY_OF_MONTH, 10);
        String tenDaysInFutureDateString = simpleDateFormat.format(tenDaysInFuture.getTime());
        return tenDaysInFutureDateString;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Date getDateObject(String expiryDateText) throws ParseException {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return simpleDateFormat.parse(expiryDateText + " 09:00:00");
        } catch (ParseException parseException ) {
            Log.i("error","Got a parse exception while parsing date: " + parseException.getMessage());
        }
        return null;
    }

    public static Calendar getCalendarInstance( int year, int month, int day ) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set( year, month, day );
        return calendar;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getLocalizedDate(Calendar calendar) {
        int style = DateFormat.MEDIUM;
        DateFormat df = DateFormat.getDateInstance(style, Locale.getDefault());
        return  df.format(calendar.getTime());
    }

    private static boolean isToday(Calendar now, Calendar calendarMayBeToday) {
        if ( now.get(Calendar.YEAR) == calendarMayBeToday.get(Calendar.YEAR) &&
                now.get(Calendar.MONTH) == calendarMayBeToday.get(Calendar.MONTH) &&
                now.get(Calendar.DAY_OF_MONTH) == calendarMayBeToday.get(Calendar.DAY_OF_MONTH) ) {
            return true;
        }
        return false;
    }
///proverka za minat datum
    public static boolean isPastDate(Calendar calendar) {
        Calendar now = Calendar.getInstance();
        if ( isToday(now, calendar) )
            return false;
        return now.after(calendar);
    }
}

