package com.example.grocerylist;

import android.os.Build;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;

import java.util.HashMap;

public class OrderAndWhere {
    Spinner spinnerSortBy;
    Spinner spinnerShowOnly;

    HashMap<Long, String> showOnlyHash;
    HashMap<Long, String> orderByHash;

    String wherePlaceholders;
    String[] whereVariables;
    String orderBy;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public OrderAndWhere(Spinner spinnerShowOnly, Spinner spinnerSortBy) {
        this.spinnerShowOnly = spinnerShowOnly;
        this.spinnerSortBy = spinnerSortBy;
        initializeOrderBy();
        initializeShowOnlyHash();
        initializeAttributes();
    }
///razlichni nachini na sortiranje
    private void initializeOrderBy() {
        this.orderByHash = new HashMap<>(6);
        this.orderByHash.put((long) 0, DataManager.TABLE_ROW_ITEM_NAME + " ASC");
        this.orderByHash.put((long) 1, DataManager.TABLE_ROW_ITEM_NAME + " DESC");
        this.orderByHash.put((long) 2, DataManager.TABLE_ROW_EXPIRY_DATE + " ASC");
        this.orderByHash.put((long) 3, DataManager.TABLE_ROW_EXPIRY_DATE + " DESC");
        this.orderByHash.put((long) 4, DataManager.TABLE_ROW_WHERE_ABOUTS + " ASC");
        this.orderByHash.put((long) 5, DataManager.TABLE_ROW_WHERE_ABOUTS + " DESC");
    }

    private void initializeShowOnlyHash() {
        this.showOnlyHash = new HashMap<>(4);
        this.showOnlyHash.put((long) 0, "SHOW_ALL_ITEMS");
        this.showOnlyHash.put((long) 1, "SHOW_ONLY_EXPIRED_ITEMS");
        this.showOnlyHash.put((long) 2, "SHOW_ONLY_NON_EXPIRED_ITEMS");
        this.showOnlyHash.put((long) 3, "SHOW_ONLY_SOON_TO_BE_EXPIRED_ITEMS");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeAttributes() {
        if ( spinnerSortBy != null ) {
            this.orderBy = orderByHash.get(spinnerSortBy.getSelectedItemId());
        }

        if ( spinnerShowOnly != null ) {
            long selectedShowOnly = spinnerShowOnly.getSelectedItemId();
            String showOnly = showOnlyHash.get(selectedShowOnly);
            switch (showOnly) {
                case "SHOW_ALL_ITEMS":
                    this.wherePlaceholders = "";
                    break;
                case "SHOW_ONLY_EXPIRED_ITEMS":
                    this.wherePlaceholders = DataManager.TABLE_ROW_EXPIRY_DATE + " <= ? ";
                    this.whereVariables = new String[]{ DateTimeUtil.getToday() };
                    break;
                case "SHOW_ONLY_NON_EXPIRED_ITEMS":
                    this.wherePlaceholders = DataManager.TABLE_ROW_EXPIRY_DATE + "> ? ";
                    this.whereVariables = new String[]{ DateTimeUtil.getToday() };
                    break;
                case "SHOW_ONLY_SOON_TO_BE_EXPIRED_ITEMS":
                    this.wherePlaceholders = DataManager.TABLE_ROW_EXPIRY_DATE + ">= ? AND " + DataManager.TABLE_ROW_EXPIRY_DATE + "<= ?";
                    this.whereVariables = new String[]{
                            DateTimeUtil.getToday(),
                            DateTimeUtil.getTenDaysInFuture()
                    };
                    break;
                default:
                    this.wherePlaceholders = "";
                    this.whereVariables = new String[0];
                    break;
            }
        }
    }

    public String getOrderBy() {
        return this.orderBy;
    }
    public String getWherePlaceholders() {
        return this.wherePlaceholders;
    }
    public String[] getWhereVariables() {
        return this.whereVariables;
    }
}

