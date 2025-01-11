package com.techcndev.istock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.techcndev.istock.Adapter.GridAdapter;
import com.techcndev.istock.Adapter.HistoryTableAdapter;
import com.techcndev.istock.DatabaseHelper.BrandDBHelper;
import com.techcndev.istock.DatabaseHelper.HistoryDBHelper;
import com.techcndev.istock.Model.HistoryModel;
import com.techcndev.istock.databinding.ActivityBrandBinding;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BrandActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    ActivityBrandBinding mainBinding;
    private static final String LOG_TAG = "BrandActivity";
    private String currentBrand, currentUser, currentDate, currentHistoryId;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    HistoryDBHelper DB;
    BrandDBHelper DBBrand;
    String price;
    SimpleDateFormat sdf = new SimpleDateFormat("MMM-yyyy");
    SimpleDateFormat input = new SimpleDateFormat("MM-dd-yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand);
        mainBinding = ActivityBrandBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);
        sharedPreferences = getSharedPreferences("PREFS_DATA", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        DB = new HistoryDBHelper(BrandActivity.this);
        DBBrand = new BrandDBHelper(BrandActivity.this);
        Intent intent = getIntent();
        String[] message = intent.getStringArrayExtra(GridAdapter.EXTRA_MESSAGE);
        currentBrand = message[0];
        currentHistoryId = message[1];
        currentUser = sharedPreferences.getString("current_user", null);
        currentUser = sharedPreferences.getString("current_user", null);
        editor.putString("current_brand", currentBrand);
        editor.commit();
        currentDate = getCurrentDate();
        price = DBBrand.getBrandPrice(currentBrand,currentUser);

        Log.d(LOG_TAG,LOG_TAG + " intent Message: " + message[0] + " <> " + message[1] + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        load_components();
    }

//    public void

    public void launch_monthyear_picker(View view) {
        MonthYearPickerDialog pickerDialog = new MonthYearPickerDialog();
                pickerDialog.setListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int i2) {
                        // function heree....
                        String dateStr = month + "-" + i2+"1" + "-" + year;
                        populate_table(dateStr);
                        mainBinding.buttonViewyear.setText(formatMonthYear(dateStr));
//                        Toast.makeText(BrandActivity.this, "monthYearStr: " + monthYearStr, Toast.LENGTH_SHORT).show();
                    }
                });
                pickerDialog.show(getSupportFragmentManager(), "MonthYearPickerDialog");
    }

    String formatMonthYear(String str) {
        Date date = null;
        try {
            date = input.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sdf.format(date);
    }

    public void load_components() {
        Spinner spinner = findViewById(R.id.qty_spinner);
        if (spinner != null) {
            spinner.setOnItemSelectedListener(this);
        }
        ArrayAdapter<CharSequence> spinner_adapter = ArrayAdapter.createFromResource(this,
                R.array.labels_array,
                android.R.layout.simple_spinner_item);
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinner != null) {
            spinner.setAdapter(spinner_adapter);
        }

        mainBinding.brandView.setText(currentBrand);
        mainBinding.brandView2.setText("Php " + price);
        mainBinding.dateTextview.setText(currentDate);
        mainBinding.buttonViewyear.setText(formatMonthYear(currentDate));
        populate_table(currentDate);
        load_dashboard();
        populate_table("");
        initiate_background_check();
    }

    public void load_dashboard() {
        // get the current history data of historyid
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String currDate = null;
        Cursor hisIDList = DB.get_all_data(currentHistoryId);
        while(hisIDList.moveToNext()) {
            String db_date = hisIDList.getString(1);
//            Log.d(LOG_TAG, "dbDate: " + db_date );
            Log.d(LOG_TAG, "1dbDate: " + db_date + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            if(currDate == null) {
                Log.d(LOG_TAG, "2dbDate: " + db_date );
                currDate = db_date;
            }

            Date current_date, check_date;
            try {

                current_date = dateFormat.parse(currDate);
                check_date = dateFormat.parse(db_date);
                Log.d(LOG_TAG, "3check_date: " + check_date + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            if(current_date.before(check_date)) {
                Log.d(LOG_TAG, "4current_date.before(check_date): " + String.valueOf(check_date) + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                Log.d(LOG_TAG, "4.2current_date.before(check_date): " + check_date.toString() + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

                // Format the date into MM-DD-YYYY
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("MM-dd-yyyy");
                String formattedDate = outputDateFormat.format(check_date);
                currDate = formattedDate;
                Log.d(LOG_TAG, "5formattedDate: " + formattedDate + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }
        }

        if(currDate == null) {
            currDate = currentDate;
        }
        Cursor cursor = DB.get_one_data(currentHistoryId,currDate);
        cursor.moveToNext();
        if(cursor.getCount() != 0) {
            String balance = cursor.getString(4);
            String sales = cursor.getString(5);

            String soldKg = "0";
            if(sales == null) {
                sales = "0";
            } else {
                if(!sales.equals("0") && !sales.equals("0.0") && !price.equals("0") && !price.equals("0.0")) {
                    soldKg = String.valueOf(Double.parseDouble(sales) / Double.parseDouble(price));
                }
            }

            String stockPeso = "0";
            if(!balance.equals("0") && !balance.equals("0.0") && !price.equals("0") && !price.equals("0.0")) {
                stockPeso = String.valueOf(Double.parseDouble(balance) * Double.parseDouble(price));
            }


            mainBinding.avaiText.setText(balance);
            mainBinding.soldText.setText(soldKg);
            mainBinding.stockText.setText(stockPeso);
            mainBinding.pesoText.setText(sales);
        }
        cursor.close();
    }

    public void initiate_background_check() {

        float ZScore = 1.96f;
        int LeadTime = 1;
        double averageDemand = 0;
        double variance = 0;
        double TotalStock = 0;
        double SafetyStock = 0;
        double ReorderPoint = 0;

        ProgressDialog progressDialog = new ProgressDialog(BrandActivity.this);
        progressDialog.setMessage("Loading data...");
        progressDialog.show();

        ArrayList<String[]> historydata = new ArrayList<>();
        List<LocalDate> weekDates =  getWeekDates();
        for(LocalDate date:weekDates) {
            // get the historyid in brand db using currentUser and brandname
            Cursor cursor = DB.get_one_data(currentHistoryId,currentDate);
            if(cursor.getCount() != 0) {
                cursor.moveToNext();
                String historyId, historyDate, historyQtyIn, historyQtyOut, historyBalance, historySales;

                historyId = cursor.getString(0);
                historyDate = cursor.getString(1);
                historyQtyIn = cursor.getString(2);
                historyQtyOut = cursor.getString(3);
                historyBalance = cursor.getString(4);
                historySales = cursor.getString(5);

                String[] data = new String[6];
                data[0] = (historyId);
                data[1] = (historyDate);
                data[2] = (historyQtyIn);
                data[3] = (historyQtyOut);
                data[4] = (historyBalance);
                data[5] = (historySales);

                historydata.add(data);
            }
        }
        averageDemand = calculateAveDemand(historydata);
        variance = calculatevariance(historydata);
        TotalStock = totalStock(historydata);
        SafetyStock = Math.sqrt(LeadTime * variance)*ZScore;
        ReorderPoint = averageDemand * LeadTime + SafetyStock;
        mainBinding.statusTextview.setText("Sufficient");
        mainBinding.stockneedText.setText("0");

        double val = 0;
        if(!Double.isNaN(TotalStock) && !Double.isNaN(ReorderPoint)) {
            val = TotalStock - ReorderPoint;

            if(val < 0) {
                mainBinding.statusTextview.setText("Insufficient");
                mainBinding.stockneedText.setText(String.valueOf(Math.abs(val)));
            }
        }


        progressDialog.dismiss();

        Log.d(LOG_TAG,"Brand in check: " + currentBrand);
        Log.d(LOG_TAG,"averageDemand: " + averageDemand);
        Log.d(LOG_TAG,"variance: " + variance);
        Log.d(LOG_TAG,"TotalStock: " + TotalStock);
        Log.d(LOG_TAG,"SafetyStock: " + SafetyStock);
        Log.d(LOG_TAG,"ReorderPoint: " + ReorderPoint);
        Log.d(LOG_TAG,"isSufficient: Insufficient");

    }

    public double totalStock(ArrayList<String[]> historydata) {
        double sum = 0;
        for(String[] data:historydata) {
            sum = sum + Double.parseDouble(data[4]);
        }
        return sum;
    }

    public double calculateAveDemand(ArrayList<String[]> historydata) {
        double sum = 0;
        for (String[] data : historydata) {
            if (data[3] != null && !data[3].equals("") && !data[3].equals("0")) {
                sum = sum + Double.parseDouble(data[3]);
            }
        }
        return sum/historydata.size();
    }

    public double calculatevariance(ArrayList<String[]> historydata) {
        double sum = 0;
        ArrayList<Double> dataIntList = new ArrayList<>();
        for (String[] data : historydata) {
            if (data[3] != null && !data[3].equals("") && !data[3].equals("0")) {
                dataIntList.add(Double.parseDouble(data[3]));
            }
        }

        for (String[] data : historydata) {
            if (data[3] != null && !data[3].equals("")) {
                sum += Double.parseDouble(data[3]);
            }
        }

        double mean = sum/historydata.size();

        double sumSquaredDifferences = 0;
        for (double value : dataIntList) {
            double difference = value - mean;
            sumSquaredDifferences += difference * difference;
        }

        // Calculate the variance
        double variance = sumSquaredDifferences / dataIntList.size();

        return variance;
    }

    public List<LocalDate> getWeekDates() {
        // Input date in the format "MM-DD-YYYY"
        String strDate = currentDate;

        // Parse the input date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        LocalDate date = LocalDate.parse(strDate, formatter);

        // Calculate the start and end dates of the week
        LocalDate startOfWeek = date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // Store the dates in a list
        List<LocalDate> weekDates = new ArrayList<>();
        LocalDate current = startOfWeek;
        while (!current.isAfter(endOfWeek)) {
            weekDates.add(current);
            current = current.plusDays(1);
        }

        return weekDates;
    }

    public void populate_table(String date) {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewBrandList);
        HistoryTableAdapter adapter = new HistoryTableAdapter(getHistoryList(date));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    private List<HistoryModel> getHistoryList(String date) {
        Cursor res = DB.get_all_data(currentHistoryId);

        ArrayList<String[]> dataList = new ArrayList<>();
        Log.d(LOG_TAG, "BrandActivity res.getCount(): " + res.getCount() + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        while (res.moveToNext()) {
            String historyId, historyDate, historyQtyIn, historyQtyOut, historyBalance, historySales;

            historyId = res.getString(0);
            historyDate = res.getString(1);
            historyQtyIn = res.getString(2);
            historyQtyOut = res.getString(3);
            historyBalance = res.getString(4);
            historySales = res.getString(5);

            String[] historyData = new String[6];
            historyData[0] = (historyId);
            historyData[1] = (historyDate);
            historyData[2] = (historyQtyIn);
            historyData[3] = (historyQtyOut);
            historyData[4] = (historyBalance);
            historyData[5] = (historySales);

            if(date.isEmpty()) {
                Log.d(LOG_TAG, "(date.isEmpty()): " + date.isEmpty());
                dataList.add(historyData);
            } else {
                // Parse the date strings to LocalDate objects
                Log.d(LOG_TAG, "date: " + date);
                Log.d(LOG_TAG, "historyDate: " + historyDate);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                LocalDate date1 = LocalDate.parse(date, formatter);
                LocalDate date2 = LocalDate.parse(historyDate, formatter);

                // Compare the year and month
                int yearComparison = date1.getYear() - date2.getYear();
                Log.d(LOG_TAG, "yearComparison: " + yearComparison);
                int monthComparison = date1.getMonthValue() - date2.getMonthValue();
                Log.d(LOG_TAG, "monthComparison: " + monthComparison);

                if (yearComparison == 0 && monthComparison == 0) {
                    dataList.add(historyData);
                } else {
                    Log.d(LOG_TAG, "This data in loop is the queried data!");
                }
            }

        }

        // sort the data
        List<String[]> sortedData;
        try {
            if(dataList.size() == 0) {
                Log.d(LOG_TAG, "The size is Zero! <>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.");
            }
            Log.d(LOG_TAG, "dataList: " + dataList + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            sortedData = sort_dates(dataList);
            for(String[] data:sortedData) {
                Log.d(LOG_TAG, "ArrayList<List<String>> sortedData: " + Arrays.toString(data) + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        List<HistoryModel> historyList = new ArrayList<>();
        for(String[] data:sortedData) {
            String qtyin = data[2];
            if(data[2] == null) {
                qtyin = "0";
            }
            String qtyout = data[3];
            if(data[3] == null) {
                qtyout = "0";
            }
            String bal = data[4];
            if(data[4] == null) {
                bal = "0";
            }
            String sales = data[5];
            if(data[5] == null) {
                sales = "0";
            }
            Log.d(LOG_TAG, "HistoryModel: " + Arrays.toString(data) + "<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.");
            historyList.add(new HistoryModel(data[0],data[1], qtyin, qtyout,bal,sales));
        }

        return historyList;
    }

    public List<String[]> sort_dates(ArrayList<String[]> data) throws ParseException {
        for(String[] data1:data) {
            Log.d(LOG_TAG, "for(List<String> data:dateStrings): " + data1);
        }

        // Define a date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

        // Create a comparator based on the date at index 0 in descending order
        Comparator<String[]> dateComparator = Comparator.comparing(arr -> {
            try {
                return dateFormat.parse(arr[1]);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Invalid date format");
            }
        }, Comparator.reverseOrder());

        // Sort the list using the custom comparator
        data.sort(dateComparator);

        // Print the sorted data
        for (String[] entry : data) {
            System.out.println(Arrays.toString(entry));
        }
        return data;
    }

    public void start_submit(View view) {
        Log.d(LOG_TAG, "start_submit started    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String inputBox = mainBinding.inputEdittext.getText().toString();
        String selectedDate = mainBinding.dateTextview.getText().toString();
        String qtyMode = mainBinding.qtySpinner.getSelectedItem().toString();
        if (!inputBox.isEmpty() && !inputBox.equals("0") && isDigit(inputBox)) {
        double curr_sales = Double.parseDouble(price) * Double.parseDouble(inputBox);
            Log.d(LOG_TAG, "start_submit inputBox: " + inputBox + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Log.d(LOG_TAG, "start_submit curr_sales" +curr_sales + "price: " + price + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        boolean isDateExist = DB.checkdata(currentHistoryId,selectedDate);
        if(isDateExist) {
            if(qtyMode.equals("Quantity In")) {
                Log.d(LOG_TAG, "start_submit Quatity In    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                // get data in db
                Cursor cursor = DB.get_one_data(currentHistoryId,selectedDate);

                // update the value of qtyIn, sales
                cursor.moveToNext();
                String qtyInStoredValue, balanceStoredValue;
                double  qtyInValue,balanceValue;
                qtyInStoredValue = cursor.getString(2);
                balanceStoredValue = cursor.getString(4);
                // update in db
                qtyInValue = Double.parseDouble(qtyInStoredValue) + Double.parseDouble(inputBox);
                balanceValue = Double.parseDouble(balanceStoredValue) + Double.parseDouble(inputBox);
                String newQtyIn = String.valueOf(qtyInValue);
                String newBalance = String.valueOf(balanceValue);
                Log.d(LOG_TAG, "start_submit Quatity In newBalance: "  +newBalance+ " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                DB.update_history(currentHistoryId, selectedDate,newQtyIn,"",newBalance,"");
                cursor.close();
            }

            if(qtyMode.equals("Quantity Out")) {
                Log.d(LOG_TAG, "start_submit Quantity Out    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                // get data in db
                Cursor cursor = DB.get_one_data(currentHistoryId, selectedDate);
                // update the value of qtyout, balance, sales
                cursor.moveToNext();
                double qtyOutStoredValue, balanceStoredValue, salesStoredValue;
                qtyOutStoredValue = 0;
                if (cursor.getString(3) != null) {
                    qtyOutStoredValue = Double.parseDouble(cursor.getString(3));
                }
                balanceStoredValue = Double.parseDouble(cursor.getString(4));
                salesStoredValue = 0;
                if(cursor.getString(5) != null) {
                    salesStoredValue = Double.parseDouble(cursor.getString(5));
                }

                if(balanceStoredValue < Double.parseDouble(inputBox)) {
                    Log.d(LOG_TAG, "balanceStoredValue < Double.parseDouble(inputBox): "  +balanceStoredValue+ " <> " + balanceStoredValue + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    negative_dialog("Error","The inputted quantity value is greater than the current balance!");
                    mainBinding.inputEdittext.setText("");
                    cursor.close();
                    return;
                }

                // update in db
                String newQtyOut = String.valueOf(qtyOutStoredValue + Double.parseDouble(inputBox));
                String newBalance = String.valueOf(balanceStoredValue - Double.parseDouble(inputBox));
                String newSales = String.valueOf(salesStoredValue + curr_sales);
                Log.d(LOG_TAG, "String.valueOf(salesStoredValue + curr_sales); " +  salesStoredValue + "<>" +salesStoredValue + "<>" + String.valueOf(salesStoredValue + curr_sales)  + "<>" + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                DB.update_history(currentHistoryId, selectedDate, "", newQtyOut, newBalance, newSales);
            }
            } else {
                // add all the balance value, the add it to the balance of new date
                if(qtyMode.equals("Quantity In")) {
                    ProgressDialog progressDialog = new ProgressDialog(BrandActivity.this);
                    progressDialog.setMessage("Validating data...");
                    progressDialog.show();
                    // get all the balance value which in the same month past dates
                    double totalBalance = 0;
                    Cursor cursor = DB.get_all_data(currentHistoryId);
                    while(cursor.moveToNext()) {
                        // query for date index
                        String db_date = cursor.getString(1);
                        String db_balance = cursor.getString(4);

                        // Define date format
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

                        try {
                            // Parse strings into Date objects
                            Date date1 = dateFormat.parse(db_date);
                            Date date2 = dateFormat.parse(selectedDate);

                            // Compare dates
                            boolean isPreviousDayMonth = date1.getMonth() == date2.getMonth() && date1.before(date2);
                            if (isPreviousDayMonth) {
                                Log.d(LOG_TAG,selectedDate + " is the previous date in the month of " + db_date);
                                totalBalance += Double.parseDouble(db_balance);
                            } else {
                                Log.d(LOG_TAG,selectedDate + " is not the previous date in the month of " + db_date);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                    double resultBal = Double.parseDouble(inputBox) + totalBalance;
                    DB.create_history(currentHistoryId, selectedDate,inputBox,"", String.valueOf(resultBal),"");
                    progressDialog.dismiss();
                }
                if(qtyMode.equals("Quantity Out")) {
                    negative_dialog("Error", "No stored stock found on this date. Stock in first!");
                    return;
                }
            }
        }
        load_dashboard();
        populate_table("");
        initiate_background_check();
    }

    public void launch_inventory(View view) {
        editor.putString("current_brand", "");
        editor.commit();
        Intent intent = new Intent(BrandActivity.this,InventoryActivity.class);
        startActivity(intent);
        finish();
    }

    public void launch_main_profile(View view) {
        editor.putString("current_brand", "");
        editor.commit();
        Intent intent = new Intent(BrandActivity.this,MainProfileActivity.class);
        startActivity(intent);
        finish();
    }

    public String getCurrentDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        return formattedDate;
    }
    public boolean isDigit(String strDigits) {
        for(char charStr:strDigits.toCharArray()) {
            Log.d(LOG_TAG,"charStr: " + charStr + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
            if(!Character.isDigit(charStr)) {

                return false;
            }
        }
        return true;
    }

    public void launch_date_picker(View view) {
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                String date = new SimpleDateFormat("MM-dd-yyy", Locale.getDefault()).format(new Date(selection));
//                Toast.makeText(BrandActivity.this, "Selected Date:" + date, Toast.LENGTH_SHORT).show();
                mainBinding.dateTextview.setText(MessageFormat.format("{0}", date));
            }
        });
        materialDatePicker.show(getSupportFragmentManager(), "tag");
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String spinnerLabel = adapterView.getItemAtPosition(i).toString();
//        displayToast(spinnerLabel);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    public void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void positive_dialog(String title, String text) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BrandActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setIcon(R.drawable.success_filled);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog_create = alertDialog.create();
        dialog_create.show();
    }

    public void negative_dialog(String title, String text) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BrandActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setIcon(R.drawable.error_solid);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog_create = alertDialog.create();
        dialog_create.show();
    }
}