package com.example.grocerylist;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.Rectangle;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private DataManager dataManager;
    private HashMap<String, CheckBox> checkedCheckBoxes = new HashMap<String, CheckBox>();
    private HashMap<String, CheckBox> unCheckedCheckBoxes = new HashMap<String, CheckBox>();
    Spinner spinnerSortBy;
    Spinner spinnerShowOnly;
    DatePickerDialog picker;
    String isoDatePattern = "yyyy-MM-dd";

    String DUPLICATE_ITEM_ERROR_MESSAGE = "This item already exists.";

    String UNIQUE_CONSTRAINT_FAILED_ERROR_MESSAGE = "UNIQUE constraint failed";
    String NO_RESULTS_FOUND = "No results found";
    String INVALID_ITEM_NAME = " is an invalid item name, item should not be empty";
    String INVALID_DATE = " is not a valid date. Please enter a valid date.";
    String PAST_DATE_ERROR = "Please enter a future date.";
    String WANT_TO_DELETE_ALL_THE_ITEMS = "Are you sure you want to delete all the items?";
    String YES = "Yes";
    String NO = "NO";

    String ITEM_NAME_STR = "Item name";
    String EXPIRY_DATE_STR = "Expiry date";
    String WHEREABOUTS_STR = "Whereabouts";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataManager = new DataManager(this);
        initializeListAndSpinners();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initializeListAndSpinners() {
        this.picker = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            OrderAndWhere orderAndWhere = new OrderAndWhere(spinnerShowOnly, spinnerSortBy);
            Cursor cursor = dataManager.getItems(orderAndWhere.getWherePlaceholders(), orderAndWhere.getWhereVariables(), orderAndWhere.getOrderBy());
            initializeItemsList(cursor);
            if (cursor.getCount() > 0) {
                initializeSpinners();
                initializeListenerForSearchView();
                initializeNotifications(cursor);
            }
        }
    }

    private void initializeSpinners() {
        spinnerSortBy = (Spinner) findViewById(R.id.spinnerSortBy);

        spinnerSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                OrderAndWhere orderAndWhere = new OrderAndWhere(spinnerShowOnly, spinnerSortBy );
                Cursor cursor = dataManager.getItems(orderAndWhere.getWherePlaceholders(), orderAndWhere.getWhereVariables(), orderAndWhere.getOrderBy());
                TableLayout tableLayout = (TableLayout) findViewById(R.id.items_list);
                if (cursor.getCount() > 0) {
                    populateItemsTable(cursor, tableLayout);
                } else {
                    populateEmptyItemsTable(tableLayout);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        spinnerShowOnly = (Spinner) findViewById(R.id.spinnerShowOnly);

        spinnerShowOnly.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                OrderAndWhere orderAndWhere = new OrderAndWhere(spinnerShowOnly, spinnerSortBy );
                Cursor cursor = dataManager.getItems(orderAndWhere.getWherePlaceholders(), orderAndWhere.getWhereVariables(), orderAndWhere.getOrderBy());

                TableLayout tableLayout = (TableLayout) findViewById(R.id.items_list);
                if (cursor.getCount() > 0) {
                    populateItemsTable(cursor, tableLayout);
                } else {
                    populateEmptyItemsTable(tableLayout);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initializeNotifications(Cursor cursor) {
        int rowCount = cursor.getCount();
        int columnCount = cursor.getColumnCount();

        cursor.moveToFirst();

        int itemIdIndex          = cursor.getColumnIndex(dataManager.TABLE_ROW_ID);
        int itemNameIndex        = cursor.getColumnIndex(dataManager.TABLE_ROW_ITEM_NAME);
        int itemExpiryDateIndex  = cursor.getColumnIndex(dataManager.TABLE_ROW_EXPIRY_DATE);
        int itemWhereAboutsIndex = cursor.getColumnIndex(dataManager.TABLE_ROW_WHERE_ABOUTS);

        for (int currentRow = 0; currentRow < rowCount; currentRow++ ) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    initializeNotification(Integer.parseInt(cursor.getString(itemIdIndex)),
                            cursor.getString(itemNameIndex),
                            cursor.getString(itemExpiryDateIndex),
                            cursor.getString(itemWhereAboutsIndex));
                }
            } catch (ParseException parseException) {
                Log.i("info", "Failed to initialize notification:\n" + parseException.getMessage());
            }
            cursor.moveToNext();
        }
    }

    private long getTimeDifferenceForNotification(Date expiryDate) {
        long timeDifference = -1;
        long currentTimeMillis = System.currentTimeMillis();
        long tenDaysAgoTimeMillis = 10 * 24 * 60 * 60 * 1000;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            timeDifference = expiryDate.getTime() - currentTimeMillis;
            if (timeDifference < 0) {
                // Item is already expired
                return -1;
            } else {
                timeDifference = expiryDate.getTime() - currentTimeMillis;
            }
        }

        return timeDifference;
    }

    private long getTimeDifferenceForTenDaysBeforeForNotification(Date expiryDate) {
        long timeDifference = -1;
        long currentTimeMillis = System.currentTimeMillis();
        long tenDaysAgoTimeMillis = 10 * 24 * 60 * 60 * 1000;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            timeDifference = expiryDate.getTime() - currentTimeMillis;
            if (timeDifference < 0) {
                // Item is already expired
                return -1;
            } else if ( timeDifference < tenDaysAgoTimeMillis ) {
                // Item is expiring before ten days, in this case send the notification
                // on the expiry date, returning -1, so that the caller knows that
                // we don't need to send a notificaiton ten days before expiry date.
                return -1;
            } else {
                // The ten day before expiry date mark is far, so we would like
                // to schedule the notification ten days before the expiry date
                long tenDaysBeforeExpiryDate = expiryDate.getTime() - tenDaysAgoTimeMillis;
                timeDifference = tenDaysBeforeExpiryDate - currentTimeMillis;
            }
        }

        return timeDifference;
    }

    private void scheduleNotification(long timeDifference, int itemId, String itemName, String expiryDate, String whereAbouts ) {

        Intent intent = new Intent(this, Notifications.class);
        intent.putExtra("itemName",    itemName);
        intent.putExtra("expiryDate",  expiryDate);
        intent.putExtra("whereAbouts", whereAbouts);
        intent.putExtra("id",          itemId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0, intent, 0 );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + timeDifference, pendingIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeNotification(int itemId, String itemName, String expiryDateText, String whereAbouts) throws ParseException {
        Date expiryDate = DateTimeUtil.getDateObject(expiryDateText);
        long timeDifference = getTimeDifferenceForNotification(expiryDate);
        if ( timeDifference == -1 ) {
            // No notifications at all because the item is in the past.
        } else {
            scheduleNotification(timeDifference, itemId, itemName, expiryDateText, whereAbouts);

            long timeDifferenceBeforeTenDays = getTimeDifferenceForTenDaysBeforeForNotification(expiryDate);
            if ( timeDifferenceBeforeTenDays == -1 ) {
                // No need to send a notification ten days before the expiry date
                // because the item will expire in less than ten days.
            } else {
                scheduleNotification(timeDifferenceBeforeTenDays, itemId, itemName, expiryDateText, whereAbouts);
            }
        }
    }

    private void cancelNotification(int itemId) {
        Intent intent = new Intent(this, Notifications.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), itemId, intent, 0);
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private void cancelNotifications(String[] itemIds) {
        for (int i = 0; i < itemIds.length; i++ ) {
            cancelNotification(Integer.parseInt(itemIds[i]));
        }
    }

    private void initializeListenerForSearchView() {
        SearchView searchView = (SearchView)findViewById(R.id.searchView);
        if ( searchView != null ) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Cursor cursor;
                    if (query.equals("")) {
                        OrderAndWhere orderAndWhere = new OrderAndWhere(spinnerShowOnly, spinnerSortBy);
                        cursor = dataManager.getItems(orderAndWhere.getWherePlaceholders(), orderAndWhere.getWhereVariables(), orderAndWhere.getOrderBy());
                    } else {
                        cursor = dataManager.searchTable(query);
                    }
                    TableLayout tableLayout = (TableLayout) findViewById(R.id.items_list);
                    if (cursor.getCount() > 0) {
                        populateItemsTable(cursor, tableLayout);
                    } else {
                        populateEmptyItemsTable(tableLayout);
                    }
                    return true;
                }

                @RequiresApi(api = Build.VERSION_CODES.N)
                public boolean onQueryTextChange(String newText) {
                    Cursor cursor;
                    if (newText.equals("")) {
                        OrderAndWhere orderAndWhere = new OrderAndWhere(spinnerShowOnly, spinnerSortBy);
                        cursor = dataManager.getItems(orderAndWhere.getWherePlaceholders(), orderAndWhere.getWhereVariables(), orderAndWhere.getOrderBy());
                    }
                    cursor = dataManager.searchTable(newText);

                    TableLayout tableLayout = (TableLayout) findViewById(R.id.items_list);
                    if (cursor.getCount() > 0) {
                        populateItemsTable(cursor, tableLayout);
                    } else {
                        populateEmptyItemsTable(tableLayout);
                    }
                    return true;
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeItemsList(Cursor cursor) {
        setContentView(R.layout.activity_main);
        TableLayout tableLayout = (TableLayout) findViewById(R.id.items_list);
        if ( cursor.getCount() > 0 ) {
            populateItemsTable(cursor, tableLayout);
        } else {
            setContentView(R.layout.empty_list);
        }
        initializeListenerForSearchView();
        cursor.moveToFirst();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void editItemOnClick(View view) {
        Set<String> checkedItems = checkedCheckBoxes.keySet();
        Iterator iterator = checkedItems.iterator();
        String itemId = (String)iterator.next();
        editItem(itemId);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void editItem(String id) {
        HashMap<String, String> item = dataManager.getItemById(id);

        setContentView(R.layout.edit_item);

        EditText itemNameEditText = (EditText)findViewById(R.id.itemName);
        itemNameEditText.setText( item.get( dataManager.TABLE_ROW_ITEM_NAME ) );

        final EditText expiryDateEditText = (EditText)findViewById(R.id.expiryDate);

        String[] tokens = item.get( dataManager.TABLE_ROW_EXPIRY_DATE ).split("-");
        final int year  = Integer.parseInt(tokens[0]);
        final int month = Integer.parseInt(tokens[1]);
        final int day   = Integer.parseInt(tokens[2]);

        expiryDateEditText.setText( DateTimeUtil.getLocalizedDate( DateTimeUtil.getCalendarInstance(year, month - 1, day ) ) );

        initializeDatePicker( expiryDateEditText, year, month - 1, day);

        expiryDateEditText.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                initializeDatePicker(expiryDateEditText, year, month - 1, day);
            }
        });

        expiryDateEditText.setOnKeyListener(new View.OnKeyListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ( keyCode != KeyEvent.KEYCODE_TAB ) {
                    initializeDatePicker(expiryDateEditText, year, month - 1, day);
                }
                return false;
            }
        });
        EditText whereAboutsEditText = (EditText)findViewById(R.id.whereAbouts);
        whereAboutsEditText.setText( item.get( dataManager.TABLE_ROW_WHERE_ABOUTS ) );

        Button button = (Button)findViewById(R.id.update_button);
        button.setTag(id);
    }

    private String getValidData(String elementName, int id) {
        EditText editText = (EditText)findViewById(id);

        String editTextText = editText.getText().toString();
        editTextText = normalizeData(editTextText);

        boolean isValidItemName = validateForNull(editTextText);

        if ( !isValidItemName ) {
            editText.setError( elementName + INVALID_ITEM_NAME);
            return null;
        } else {
            return editTextText;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getExpiryDate() {
        EditText expiryDateEditText = (EditText) findViewById(R.id.expiryDate);
        String expiryDateString = expiryDateEditText.getText().toString();

        boolean isValidExpiryDate = validateForNull(expiryDateString);
        if (!isValidExpiryDate) {
            expiryDateEditText.setError(expiryDateString + INVALID_DATE);
            return null;
        }

        expiryDateString = normalizeData(expiryDateString);

        DatePicker datePicker = picker.getDatePicker();
        int year        = datePicker.getYear();
        int month       = datePicker.getMonth();
        int dayOfMonth  = datePicker.getDayOfMonth();

        Calendar expiryDateCalendar = Calendar.getInstance();
        expiryDateCalendar.set(year, month, dayOfMonth);

        if ( DateTimeUtil.isPastDate(expiryDateCalendar) ) {
            expiryDateEditText.setError(expiryDateString + PAST_DATE_ERROR);
            return null;
        }

        Date expiryDate = expiryDateCalendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(expiryDate);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateItem(View view) {
        String itemName = getValidData(ITEM_NAME_STR, R.id.itemName);
        String expiryDate = getExpiryDate();
        String whereAbouts = getValidData(WHEREABOUTS_STR, R.id.whereAbouts);

        if ( itemName != null && expiryDate != null && whereAbouts != null ) {
            Button button = (Button) findViewById(R.id.update_button);
            String id = (String) button.getTag();
            try {
                dataManager.updateItem(id, itemName, expiryDate, whereAbouts);
                int intId = Integer.parseInt(id);
                cancelNotification(intId);
                initializeNotification(intId, itemName, expiryDate, whereAbouts);
                initializeListAndSpinners();
            } catch (SQLException sqlException) {
                String errorMessage = sqlException.getMessage();
                if ( errorMessage.contains(UNIQUE_CONSTRAINT_FAILED_ERROR_MESSAGE)) {
                    showError(DUPLICATE_ITEM_ERROR_MESSAGE);
                } else {
                    showError(sqlException.getMessage());
                }
            } catch (ParseException parseException) {
                Log.i("debug", "Failed to initialize notification : " + parseException.getMessage());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void deleteItemsOnClick(View view) {
        Object[] checkedItemsArray = checkedCheckBoxes.keySet().toArray();
        final String[] checkedItemsIds = Arrays.copyOf(checkedItemsArray, checkedItemsArray.length, String[].class);
        if ( dataManager.getCountAll() == checkedItemsIds.length ) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            dataManager.deleteItems(checkedItemsIds);
                            cancelNotifications(checkedItemsIds);
                            initializeListAndSpinners();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            initializeListAndSpinners();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage(WANT_TO_DELETE_ALL_THE_ITEMS).setPositiveButton(YES, dialogClickListener)
                    .setNegativeButton(NO, dialogClickListener).show();
        } else {
            dataManager.deleteItems(checkedItemsIds);
            cancelNotifications(checkedItemsIds);
            initializeListAndSpinners();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void deleteItem(String id) {
        dataManager.deleteItem(id);
        cancelNotification(Integer.parseInt(id));
        initializeListAndSpinners();
    }

    private void cleanTableWithHeader(TableLayout table) {
        table.removeViews(0, table.getChildCount());
    }

    private void cleanTable(TableLayout table) {

        int childCount = table.getChildCount();

        // Remove all rows except the first one
        if (childCount > 1) {
            table.removeViews(1, childCount - 1);
        }
    }

    private void populateEmptyItemsTable(TableLayout tableLayout) {
        cleanTableWithHeader(tableLayout);
        TableRow tableRow = new TableRow(tableLayout.getContext());
        TextView textView = new TextView(tableLayout.getContext());
        textView.setText(NO_RESULTS_FOUND);
        textView.setPadding(10, 6, 10, 6);
        tableRow.addView(textView);
        tableLayout.addView(tableRow);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    private void populateItemsTable(Cursor cursor, TableLayout tableLayout ) {
        cleanTableWithHeader(tableLayout);
        TableRow tableRow;
        TextView textView;

        final int rowCount = cursor.getCount();
        int columnCount = cursor.getColumnCount();

        cursor.moveToFirst();

        tableRow = new TableRow(tableLayout.getContext());
        String[] headers = new String[]{ ITEM_NAME_STR, EXPIRY_DATE_STR, WHEREABOUTS_STR};

        Resources res = getResources();

        Drawable borderRow = ResourcesCompat.getDrawable(res, R.drawable.border, getTheme());

        final CheckBox checkBox = new CheckBox(tableLayout.getContext());
        checkBox.setId(R.id.selectAllItems);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDeselectAllItems(v);
            }
        });
        checkBox.setPadding(0, 0, 5, 0);
        tableRow.addView(checkBox);
        tableRow.setBackground(borderRow);

        for (int i = 0; i < headers.length; i++ ) {
            SpannableString spanString = new SpannableString(headers[i]);
            spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
            textView = new TextView(tableLayout.getContext());
            textView.setText(spanString);
            textView.setPadding(5, 0, 5, 0);

            tableRow.addView(textView);
        }

        tableLayout.addView(tableRow);
        unCheckedCheckBoxes.clear();
        checkedCheckBoxes.clear();
        for (int currentRow = 0; currentRow < rowCount; currentRow++ ) {
            tableRow = new TableRow(tableLayout.getContext());

            String expiryDate = cursor.getString( cursor.getColumnIndex(DataManager.TABLE_ROW_EXPIRY_DATE) );
            expiryDate = normalizeData(expiryDate);

            String[] tokens = expiryDate.split("-");
            int year  = Integer.parseInt(tokens[0]);
            int month = Integer.parseInt(tokens[1]);
            int day   = Integer.parseInt(tokens[2]);

            Calendar expiryDateCalendar = DateTimeUtil.getCalendarInstance(year, month - 1, day);
            String localizedExpiryDate = DateTimeUtil.getLocalizedDate(expiryDateCalendar);

            boolean isExpired = false;
            if ( expiryDateCalendar != null && DateTimeUtil.isPastDate(expiryDateCalendar) ) {
                isExpired = true;
            }
            final CheckBox checkBoxPerItem = new CheckBox(tableLayout.getContext());
            final String itemId = cursor.getString( cursor.getColumnIndex(DataManager.TABLE_ROW_ID) );
            checkBoxPerItem.setTag(itemId);
            checkBoxPerItem.setPadding(0, 0, 5, 0);

            checkBoxPerItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBoxPerItem.isChecked()) {
                        checkedCheckBoxes.put(itemId, checkBoxPerItem);
                        if (unCheckedCheckBoxes.containsValue(checkBoxPerItem)) {
                            unCheckedCheckBoxes.remove(itemId, checkBoxPerItem);
                        }
                    } else {
                        unCheckedCheckBoxes.put(itemId, checkBoxPerItem);
                        if (checkedCheckBoxes.containsValue(checkBoxPerItem)) {
                            checkedCheckBoxes.remove(itemId, checkBoxPerItem);
                        }

                    }
                    if ( checkedCheckBoxes.size() == rowCount ) {
                        checkBox.setChecked(true);
                    } else {
                        checkBox.setChecked(false);
                    }

                    enableDisableEditAdd();
                }
            });
            unCheckedCheckBoxes.put(itemId, checkBoxPerItem);
            enableDisableEditAdd();
            tableRow.addView(checkBoxPerItem);
            for (int currentColumn = 1; currentColumn < columnCount; currentColumn++) {
                String data = cursor.getString(currentColumn);
                textView = new TextView(tableLayout.getContext());

                if ( currentColumn == cursor.getColumnIndex(DataManager.TABLE_ROW_EXPIRY_DATE) ) {
                    textView.setText(localizedExpiryDate);
                } else {
                    textView.setText(data);
                }
                textView.setPadding(10, 6, 10, 6);
                if ( isExpired ) {
                    textView.setTextColor(Color.RED);
                }

                tableRow.addView(textView);
            }

            if ( currentRow == rowCount - 1 ) {
                Drawable lastBorderRow = ResourcesCompat.getDrawable(res, R.drawable.table_row_last_bg, getTheme());
                tableRow.setBackground(lastBorderRow);
            } else {
                tableRow.setBackground(borderRow);
            }
            tableLayout.addView(tableRow);

            cursor.moveToNext();
        }
    }

    public void selectDeselectAllItems(View view) {
        CheckBox checkBox = findViewById(R.id.selectAllItems);
        boolean isChecked = checkBox.isChecked();

        if ( isChecked ) {
            for (Map.Entry<String, CheckBox> entry : unCheckedCheckBoxes.entrySet()) {
                String itemId = entry.getKey();
                CheckBox checkBoxPerItem = entry.getValue();
                checkBoxPerItem.setChecked(isChecked);
                checkedCheckBoxes.put(itemId, checkBoxPerItem);
            }
            unCheckedCheckBoxes.clear();
        } else {
            for (Map.Entry<String, CheckBox> entry : checkedCheckBoxes.entrySet()) {
                String itemId = entry.getKey();
                CheckBox checkBoxPerItem = entry.getValue();
                checkBoxPerItem.setChecked(isChecked);
                unCheckedCheckBoxes.put(itemId, checkBoxPerItem);
            }
            checkedCheckBoxes.clear();
        }

        enableDisableEditAdd();
    }

    private void enableEditButton(boolean enable) {
        ImageButton editItemButton = findViewById(R.id.edit_item_button);
        editItemButton.setEnabled(enable);
    }

    private void enableAddButton(boolean enable) {
        ImageButton addItemButton = findViewById(R.id.add_item_button);
        addItemButton.setEnabled(enable);
    }

    public void enableDisableEditAdd() {
        int size = checkedCheckBoxes.size();
        if ( size == 0 ) {
            enableEditButton(false);
            enableAddButton(true);
        } else if ( size == 1 ) {
            enableEditButton(true);
            enableAddButton(false);
        } else if ( size > 1 ) {
            enableEditButton(false);
            enableAddButton(false);
        }
    }

    public void addItem(View view) {
        setContentView(R.layout.add_item_popup);
        final EditText expiryDateEditText = (EditText) findViewById(R.id.expiryDate);
        expiryDateEditText.setInputType(InputType.TYPE_NULL);
        expiryDateEditText.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                initializeDatePicker(expiryDateEditText, 0, 0, 0);
            }
        });

        expiryDateEditText.setOnKeyListener(new View.OnKeyListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ( keyCode != KeyEvent.KEYCODE_TAB ) {
                    initializeDatePicker(expiryDateEditText, 0, 0, 0);
                }
                return false;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeDatePicker(final EditText expiryDateEditText, int expiryYear, int expiryMonth, int expiryDay) {
        final Calendar cldr = Calendar.getInstance();
        int day;
        if ( expiryDay == 0 ) {
            day = cldr.get(Calendar.DAY_OF_MONTH);
        } else {
            day = expiryDay;
        }

        final int month;
        if (expiryMonth == 0 ) {
            month = cldr.get(Calendar.MONTH);
        } else {
            month = expiryMonth;
        }

        int year;
        if (expiryYear == 0 ) {
            year = cldr.get(Calendar.YEAR);
        } else {
            year = expiryYear;
        }

        // date picker dialog
        if ( picker == null ) {
            picker = new DatePickerDialog(MainActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            Calendar calendar = Calendar.getInstance(Locale.getDefault());
                            calendar.set(year, monthOfYear, dayOfMonth);
                            int style = DateFormat.MEDIUM;
                            DateFormat df = DateFormat.getDateInstance(style, Locale.getDefault());
                            String formattedDate = df.format(calendar.getTime());
                            expiryDateEditText.setText(formattedDate);
                        }
                    }, year, month, day);
        }
        picker.getDatePicker().setMinDate(System.currentTimeMillis());
        picker.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void cancelAddItem(View view) {
        initializeListAndSpinners();
    }

    private String normalizeData(String data) {
        return data.trim();
    }

    private boolean validateForNull(String data) {
        if ( data == null ) {
            return false;
        } else return !data.equals("");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void saveItem(View view) {
        String itemName = getValidData(ITEM_NAME_STR, R.id.itemName);
        String expiryDate = getExpiryDate();
        String whereAbouts = getValidData( WHEREABOUTS_STR, R.id.whereAbouts );

        long itemId = 0;
        if ( itemName != null && expiryDate != null && whereAbouts != null ) {
            try {
                itemId = dataManager.insert(itemName, expiryDate, whereAbouts);

                if ( itemId > 0) {
                    initializeNotification((int) itemId, itemName, expiryDate, whereAbouts);
                }
                initializeListAndSpinners();

            } catch (SQLException sqlException ) { // Triggered by insert
                String errorMessage = sqlException.getMessage();
                if ( errorMessage.contains(UNIQUE_CONSTRAINT_FAILED_ERROR_MESSAGE)) {
                    showError(DUPLICATE_ITEM_ERROR_MESSAGE);
                } else {
                    Log.i("debug", errorMessage);
                    showError(errorMessage);
                }
            } catch (ParseException parseException) { // Triggered by initializeNotification
                Log.i("debug", "Failed to initialize notification : \n" + parseException.getMessage());
            }
        }
    }


    private void showError( String message ) {
        EditText expiryDateEditText = (EditText) findViewById(R.id.expiryDate);
        expiryDateEditText.setError(message);
        EditText itemNameEditText = (EditText) findViewById(R.id.itemName);
        itemNameEditText.setError(message);
        EditText whereAboutsEdit = (EditText) findViewById(R.id.whereAbouts);
        whereAboutsEdit.setError(message);
    }
}
