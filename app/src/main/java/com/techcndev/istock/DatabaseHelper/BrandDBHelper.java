package com.techcndev.istock.DatabaseHelper;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.techcndev.istock.InventoryActivity;
import com.techcndev.istock.R;

import java.util.Arrays;

public class BrandDBHelper extends SQLiteOpenHelper {

    String LOG_TAG = "AccountHelper";
    Context app_context;
    SharedPreferences sharedPreferences;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    SharedPreferences.Editor editor;
    BrandDBHelper DB;
    String currentUser;

    public BrandDBHelper(Context context) {
        super(context, "brandDB.db", null, 1);
        app_context = context;
        sharedPreferences = app_context.getSharedPreferences("PREFS_DATA", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        auth = FirebaseAuth.getInstance();
        currentUser = sharedPreferences.getString("current_user", null);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table brand(brand_name TEXT primary key, " +
                                            "history_id TEXT , account_user TEXT, " +
                                            "brand_price TEXT, brand_stock TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("drop Table if exists brand");
    }

    public Boolean save_brand(String brandName, String historyId,
                                String accountUser, String brandPrice,
                                String brandStock) {
        SQLiteDatabase DB;
        Cursor cursor;
        ProgressDialog progressDialog = new ProgressDialog(app_context);
        progressDialog.setMessage("Saving brand...");
        progressDialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }, 3000);
        try {
            DB = this.getWritableDatabase();
            String[] whereArgs = {brandName,accountUser};
            cursor = DB.rawQuery("Select * from brand where brand_name = ? AND account_user = ?", whereArgs);
        } catch (Exception e) {
            Toast.makeText(app_context, "Unable to process: Database initialization error!", Toast.LENGTH_LONG).show();
            return false;
        }
        if(cursor.getCount() == 0) {
            DB = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            if(brandName != null && !brandName.isEmpty()) {
                contentValues.put("brand_name", brandName);
            }
            if(historyId != null && !historyId.isEmpty()) {
                contentValues.put("history_id", historyId);
            }
            if(accountUser != null && !accountUser.isEmpty()) {
                contentValues.put("account_user", accountUser);
            }
            if(brandPrice != null && !brandPrice.isEmpty()) {
                contentValues.put("brand_price", brandPrice);
            }
            if(brandStock != null && !brandStock.isEmpty()) {
                contentValues.put("brand_stock", brandStock);
            }

            long result = DB.insert("brand", null, contentValues);

            if (result == -1) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(app_context);
                alertDialog.setTitle("Brand Adding");
                alertDialog.setMessage("DB Error: Adding failed");
                alertDialog.setIcon(R.drawable.error_solid);
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog_create = alertDialog.create();
                progressDialog.dismiss();
                dialog_create.show();
            } else {
                cursor.close();
                positive_dialog("Brand Adding", "Brand successfully added!");
                return true;
            }
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(app_context);
            alertDialog.setTitle("Brand Adding");
            alertDialog.setMessage("Brand already exists!");
            alertDialog.setIcon(R.drawable.error_solid);
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog_create = alertDialog.create();
            progressDialog.dismiss();
            dialog_create.show();
        }
        cursor.close();
        progressDialog.dismiss();
        return false;
    }

    public Boolean update_brand(String brandName, String accountUser, String brandPrice,
                                String brandStock) {

        Log.d(LOG_TAG,"After data DBHELPER: " + " email:" + brandName + " activeName:" + brandPrice + ">>>>>>>>>>>>>>>>>>>>>>>>>");
        ProgressDialog progressDialog = new ProgressDialog(app_context);
        progressDialog.setMessage("Updating account information...");
        progressDialog.show();
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        if (brandName != null && !brandName.isEmpty()) {
            contentValues.put("account_user", brandName);
        }
        if (brandPrice != null && !brandPrice.isEmpty()) {
            contentValues.put("account_email", brandPrice);
        }
        if (brandStock != null && !brandStock.isEmpty()) {
            contentValues.put("account_phone", brandStock);
        }

        String[] whereArgs = {brandName,accountUser};
        Cursor cursor = DB.rawQuery("Select * from brand where brand_name = ? AND account_user = ?", whereArgs);
        //if has match, update data
        if (cursor.getCount() > 0 || auth.getCurrentUser() != null) {
            long result = DB.update("brand", contentValues, "brand_name=? AND account_user=?", whereArgs);
            cursor.close();
            if (result == -1) {
                progressDialog.dismiss();
                negative_dialog("","Failed!");
                return false;
            } else {
                progressDialog.dismiss();
                positive_dialog("","Success");
                return true;
            }
        }
        progressDialog.dismiss();
        return false;
    }

    public boolean delete_brand(String brandName, String accountUser) {
        ProgressDialog progressDialog = new ProgressDialog(app_context);
        progressDialog.setMessage("Saving brand...");
        progressDialog.show();
        SQLiteDatabase DB = this.getWritableDatabase();
        String[] whereArgs = {brandName,accountUser};
        Cursor cursor = DB.rawQuery("Select * from brand where brand_name = ? AND account_user = ?", whereArgs);
        if (cursor.getCount() > 0) {
            long result = DB.delete("brand", "brand_name=? AND account_user=?", whereArgs);

            if (result == -1) {
                Log.d(LOG_TAG, "Delete Error...");
                progressDialog.dismiss();
                cursor.close();
                return false;
            } else {
                cursor.close();
                progressDialog.dismiss();
                return true;
            }
        } else {
            progressDialog.dismiss();
            cursor.close();
            return false;
        }
    }

    public Cursor get_all_data() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from brand where account_user = ?", new String[]{currentUser});
        return cursor;
    }

    public Cursor get_one_data(String brandName) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from brand where brand_name = ?", new String[]{brandName});
        Log.d(LOG_TAG,"get_one_data: " + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    //checks if the data is existing using given string
    public Boolean checkdata(String brandName) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from brand where brand_name = ?", new String[]{brandName});
        if (cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean check_history_id(String historyId) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from brand where history_id = ?", new String[]{historyId});
        if (cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getHistoryId(String brandName, String accountUser) {
        SQLiteDatabase DB = this.getWritableDatabase();
        String[] whereArgs = {brandName,accountUser};
        Cursor cursor = DB.rawQuery("Select * from brand where brand_name = ? AND account_user = ?", whereArgs);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            String id = cursor.getString(1);
            return id;
        } else {
            return "";
        }
    }

    public String getBrandPrice(String brandName, String accountUser) {
        SQLiteDatabase DB = this.getWritableDatabase();
        String[] whereArgs = {brandName,accountUser};
        Cursor cursor = DB.rawQuery("Select * from brand where brand_name = ? AND account_user = ?", whereArgs);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            String price = cursor.getString(3);
            return price;
        } else {
            return "";
        }
    }

    public void positive_dialog(String title, String text) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(app_context);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(app_context);
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