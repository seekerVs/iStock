package com.techcndev.istock;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SearchView;

import com.techcndev.istock.Adapter.GridAdapter;
import com.techcndev.istock.DatabaseHelper.BrandDBHelper;
import com.techcndev.istock.DatabaseHelper.HistoryDBHelper;
import com.techcndev.istock.databinding.ActivityInventoryBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class InventoryActivity extends AppCompatActivity {

    final String LOG_TAG = "InventoryActivity";
    ActivityInventoryBinding mainBinding;
    BrandDBHelper DB;
    HistoryDBHelper DBHistory;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public String currentDate;
    public String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        Log.d(LOG_TAG, "InventoryActivity onCreate: " + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        mainBinding = ActivityInventoryBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);
        sharedPreferences = getSharedPreferences("PREFS_DATA", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        currentUser = sharedPreferences.getString("current_user", null);
        DB = new BrandDBHelper(InventoryActivity.this);
        DBHistory = new HistoryDBHelper(InventoryActivity.this);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        Log.d(LOG_TAG,"InventoryActivity intentmessage: " + message + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        if (message != null) {
            String dialogText = "Log in successful!";
            positive_dialog("Log In", dialogText);
        }

        mainBinding.searchview1.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                    populate_gridview(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
            populate_gridview("");
    }

    public String capitalizeWord(String str){
        String[] words =str.split("\\s");
        String capitalizeWord="";
        for(String w:words){
            String first=w.substring(0,1);
            String afterfirst=w.substring(1);
            capitalizeWord+=first.toUpperCase()+afterfirst+" ";
        }
        return capitalizeWord.trim();
    }

    private void populate_gridview(String searchKey) {
        Cursor res = DB.get_all_data();
        int length = 0;
        if(!searchKey.equals("")) {
            searchKey = capitalizeWord(searchKey);
            length = 1;
        } else {
             length = res.getCount();
        }
        String[] brandName = new String[length];
        String[] balanceKg = new String[length];
        int counter = 0;
        int contentCounter = 0;
        while(res.moveToNext()) {
            String val1 = res.getString(0);
            String val2 = res.getString(1);
            String val3 = res.getString(2);
            String val4 = res.getString(3);
            String val5 = res.getString(4);

            if(!searchKey.equals("")) {
                if(searchKey.equals(val1)) {
                    brandName[counter] = val1;
                    balanceKg[counter] = val5;
                    contentCounter++;
                } else {
                    continue;
                }
            }

            brandName[counter] = val1;
            balanceKg[counter] = val5;
            counter++;

            Log.d(LOG_TAG, "ROW " + " val1: "+ val1 + " val2: " + val2 + " val3: " + val3 + " val4: " + val4 + " val5: " + val5);
        }
        Log.d(LOG_TAG, "Arrays.asList(brandName).isEmpty(): " + Arrays.asList(brandName).isEmpty() + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        if (contentCounter == 0 && !searchKey.equals("")) {
            positive_dialog("Brand Search", "Found no matches for the keyword in the database");
            return;
        }
        res.close();
        GridView gridView = findViewById(R.id.gridView);
        GridAdapter gridAdapter = new GridAdapter(InventoryActivity.this,brandName,balanceKg);
        gridView.setAdapter(gridAdapter);
//        gridAdapter.notifyDataSetChanged();
        gridAdapter.addGridButton();
        startCounterService();
    }

    private void startCounterService() {
        try {
            String status = sharedPreferences.getString("is_notif_enabled", null);
            Log.d(LOG_TAG, "InventoryActivity isNotifEnabled: " + status + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            if (status == null || status.equals("true")) {
//            editor.putString("isNotifEnabled", "false");
//            editor.commit();
                Context context = getApplicationContext();
                Intent intent = new Intent(this, ForegroundService.class);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent);
                } else {
                    context.startService(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void launch_profile(View view) {
        Intent intent = new Intent(InventoryActivity.this,MainProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void positive_dialog(String title, String text) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(InventoryActivity.this);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(InventoryActivity.this);
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

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "InventoryActivity onStart: " + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "InventoryActivity onStop: " + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "InventoryActivity onDestroy: " + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "InventoryActivity onPause: " + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(LOG_TAG, "InventoryActivity onRestart: " + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Override
    protected void onResume() {
        super.onResume();
        populate_gridview("");
        Log.d(LOG_TAG, "InventoryActivity onResume: " + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
}