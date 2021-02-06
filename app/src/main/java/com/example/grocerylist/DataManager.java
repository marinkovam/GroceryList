package com.example.grocerylist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class DataManager {
    // This is the actual database
    private final SQLiteDatabase db;

    public DataManager(Context context) {
        // Create an instance of our internal CustomSQLiteOpenHelper

        CustomSQLiteOpenHelper helper = new
                CustomSQLiteOpenHelper(context);

        // Get a writable database
        db = helper.getWritableDatabase();
    }
    /*
        Next we have a public static final string for
        each row/table that we need to refer to both
        inside and outside this class
    */

    public static final String TABLE_ROW_ID = "_id";
    public static final String TABLE_ROW_ITEM_NAME = "item_name";
    public static final String TABLE_ROW_EXPIRY_DATE = "expiry_date";
    public static final String TABLE_ROW_WHERE_ABOUTS = "where_about";

    /*
        Next we have a private static final strings for
        each row/table that we need to refer to just
        inside this class
    */

    private static final String DB_NAME = "grocery_manager";
    private static final int DB_VERSION = 1;
    private static final String TABLE_ITEMS = "items";

    // Insert a record
    public long insert(String itemName, String expiryDate, String whereAbouts ) throws SQLException {
        Log.i("assert", "in insert");
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_ROW_ITEM_NAME, itemName);
        contentValues.put(TABLE_ROW_EXPIRY_DATE, expiryDate);
        contentValues.put(TABLE_ROW_WHERE_ABOUTS, whereAbouts);
        return db.insertOrThrow(TABLE_ITEMS, null, contentValues );
    }

    public int getCountAll() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) as total_count" + " from " +
                TABLE_ITEMS, null);
        cursor.moveToFirst();
        int count = cursor.getInt( cursor.getColumnIndex("total_count") );
        return count;
    }

    public Cursor selectAll() {
        Cursor cursor = db.rawQuery("SELECT *" + " from " +
                TABLE_ITEMS + " order by " + TABLE_ROW_ITEM_NAME, null);
        return cursor;
    }

    public Cursor searchTable(String query) {
        String likeQuery = "%" + query + "%";
        Cursor cursor = db.rawQuery("SELECT * " + "from " +
                TABLE_ITEMS + " WHERE " +
                TABLE_ROW_ITEM_NAME + " LIKE ? OR " +
                TABLE_ROW_EXPIRY_DATE + " LIKE ? OR " +
                TABLE_ROW_WHERE_ABOUTS + " LIKE ?" +
                "order by " + TABLE_ROW_ITEM_NAME, new String[]{ likeQuery, likeQuery, likeQuery });
        return cursor;
    }

    public HashMap<String, String> getItemById(String id) {
        String[] columns = new String[] { TABLE_ROW_ITEM_NAME, TABLE_ROW_EXPIRY_DATE, TABLE_ROW_WHERE_ABOUTS};
        Cursor cursor = db.query(TABLE_ITEMS, columns, TABLE_ROW_ID + " = ?", new String[]{ id }, null, null, null);
        HashMap<String, String> item = new HashMap<>();

        cursor.moveToFirst();
        item.put(TABLE_ROW_ITEM_NAME, cursor.getString(0));
        item.put(TABLE_ROW_EXPIRY_DATE, cursor.getString(1));
        item.put(TABLE_ROW_WHERE_ABOUTS, cursor.getString(2));
        return item;
    }

    public void updateItem(String id, String itemName, String expiryDate, String whereAbouts) throws SQLiteConstraintException {
        ContentValues contentValues = new ContentValues(2);
        contentValues.put(TABLE_ROW_ITEM_NAME, itemName);
        contentValues.put(TABLE_ROW_EXPIRY_DATE, expiryDate);
        contentValues.put(TABLE_ROW_WHERE_ABOUTS, whereAbouts);
        db.update(TABLE_ITEMS, contentValues, TABLE_ROW_ID + " = ? ", new String[]{id} );
    }

    public void deleteItem(String id) {
        db.delete(TABLE_ITEMS, TABLE_ROW_ID + " = ? ", new String[]{id} );
    }

    public void deleteItems(String[] ids) {
        String placeHolder = "";
        for (int i = 0; i < ids.length - 1; i++ ) {
            placeHolder += "?,";
        }
        placeHolder += "?";
        db.delete(TABLE_ITEMS, TABLE_ROW_ID + " IN (" + placeHolder + ")", ids );
    }

    public Cursor getItems(String where, String[] whereBindVariables, String orderBy) {
        return db.query(TABLE_ITEMS, null, where, whereBindVariables, null, null, orderBy);
    }

    private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
        public CustomSQLiteOpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        // This runs the first time the database is created
        @Override
        public void onCreate(SQLiteDatabase db) {

            // Create a table for photos and all their details
            String newTableQueryString = "create table "
                    + TABLE_ITEMS + " ("
                    + TABLE_ROW_ID
                    + " integer primary key autoincrement not null, "
                    + TABLE_ROW_ITEM_NAME
                    + " text not null,"
                    + TABLE_ROW_EXPIRY_DATE
                    + " date not null,"
                    + TABLE_ROW_WHERE_ABOUTS
                    + " text not null default '', "
                    + "unique(" + TABLE_ROW_ITEM_NAME + ","
                    + TABLE_ROW_EXPIRY_DATE + ","
                    + TABLE_ROW_WHERE_ABOUTS + ") );";

            db.execSQL(newTableQueryString);
        }

        // This method only runs when we increment DB_VERSION
        @Override
        public void onUpgrade(SQLiteDatabase db,
                              int oldVersion, int newVersion) {
            // Not needed in this app
            // but we must still override it

        }

    }
}

