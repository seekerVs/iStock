package com.techcndev.istock.DatabaseHelper;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.techcndev.istock.R;

import java.util.Arrays;

public class HistoryDBHelper extends SQLiteOpenHelper {

    String LOG_TAG = "AccountHelper";
    Context app_context;
    SharedPreferences sharedPreferences;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    SharedPreferences.Editor editor;

    public HistoryDBHelper(Context context) {
        super(context, "historyDB.db", null, 3);
        app_context = context;
        sharedPreferences = app_context.getSharedPreferences("PREFS_DATA", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table history(history_id TEXT, " +
                                            "history_date TEXT , quantity_in TEXT, " +
                                            "quantity_out TEXT, history_balance TEXT, history_sales TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("drop Table if exists history");
    }

    public boolean create_history(String historyId, String historyDate,
                                String quantityIn, String quantityOut,
                                String historyBalance, String historySales) {
        SQLiteDatabase DB;
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
        } catch (Exception e) {
            Toast.makeText(app_context, "Unable to process: Database initialization error!", Toast.LENGTH_LONG).show();
            return false;
        }

        Log.d(LOG_TAG, "historyId: " + historyId + " historyDate: " + historyDate + " quantityIn: " + quantityIn + " quantityOut: " + quantityOut + " historyBalance: " + historyBalance + " historySales: " + historySales);

        String[] whereArgs = {historyId, historyDate};
        Cursor cursor = DB.rawQuery("Select * from history where history_id = ? AND history_date = ?", whereArgs);
        if(cursor.getCount() == 0) {
            DB = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("history_id", historyId);
            contentValues.put("history_date", historyDate);
            if(quantityIn != null && !quantityIn.isEmpty()) {
                contentValues.put("quantity_in", quantityIn);
            }
            if(quantityOut != null && !quantityOut.isEmpty()) {
                contentValues.put("quantity_out", quantityOut);
            }
            if(historyBalance != null && !historyBalance.isEmpty()) {
                contentValues.put("history_balance", historyBalance);
            }
            if(historySales != null && !historySales.isEmpty()) {
                contentValues.put("history_sales", historySales);
            }

            long result = DB.insert("history", null, contentValues);

            if (result == -1) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(app_context);
                alertDialog.setTitle("Brand History Query");
                alertDialog.setMessage("DB Error: DB query failed");
                alertDialog.setIcon(R.drawable.error_solid);
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog_create = alertDialog.create();
                progressDialog.dismiss();
                dialog_create.show();
                return false;
            } else {
                cursor.close();
                positive_dialog("Brand History Query", "Action successfully completed!");
                return true;
            }
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(app_context);
            alertDialog.setTitle("Brand History Query");
            alertDialog.setMessage("ID Error: Contact developer");
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

    public boolean update_history(String historyId, String historyDate,
                                  String quantityIn, String quantityOut,
                                  String historyBalance, String historySales) {

        ProgressDialog progressDialog = new ProgressDialog(app_context);
        progressDialog.setMessage("Updating account information...");
        progressDialog.show();
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        if (quantityIn != null && !quantityIn.isEmpty()) {
            contentValues.put("quantity_in", quantityIn);
        }
        if (quantityOut != null && !quantityOut.isEmpty()) {
            contentValues.put("quantity_out", quantityOut);
        }
        if (historyBalance != null && !historyBalance.isEmpty()) {
            contentValues.put("history_balance", historyBalance);
        }
        if (historySales != null && !historySales.isEmpty()) {
            contentValues.put("history_sales", historySales);
        }

        String[] whereArgs = {historyId, historyDate};
        Cursor cursor = DB.rawQuery("Select * from history where history_id = ? AND history_date = ?", whereArgs);
        Log.d(LOG_TAG, "update " + Arrays.toString(cursor.getColumnNames()) + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        //if has match, update data
        if (cursor.getCount() > 0) {
            long result = DB.update("history", contentValues, "history_id = ? AND history_date = ?", whereArgs);
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

    public Boolean delete_history(String historyId, String historyDate) {
        SQLiteDatabase DB = this.getWritableDatabase();
        String[] whereArgs = {historyId, historyDate};
        Cursor cursor = DB.rawQuery("Select * from history where history_id = ? AND history_date = ?", whereArgs);
        if (cursor.getCount() > 0) {
            long result = DB.delete("history", "history_id = ? AND history_date = ?", whereArgs);
            if (result == -1) {
                negative_dialog("History DB Query", "Error: Deletion failed!");
                Log.d(LOG_TAG, "Error: Deletion failed!");
                return false;
            } else {
                cursor.close();
                return true;
            }
        } else {
            return false;
        }
    }

    public Cursor get_all_data(String historyId) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from history where history_id = ?", new String[]{historyId});
        return cursor;
    }

    public Cursor get_one_data(String historyId, String historyDate) {
        SQLiteDatabase DB = this.getWritableDatabase();
        String[] whereArgs = {historyId, historyDate};
        Cursor cursor = DB.rawQuery("Select * from history where history_id = ? AND history_date = ?", whereArgs);
        Log.d(LOG_TAG,"get_one_data: " + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    //checks if the data is existing using given string
    public boolean checkdata(String historyId, String historyDate) {
        SQLiteDatabase DB = this.getWritableDatabase();
        String[] whereArgs = {historyId, historyDate};
        Cursor cursor = DB.rawQuery("Select * from history where history_id = ? AND history_date = ?", whereArgs);
        if (cursor.getCount() > 0) {
            return true;
        } else {
            return false;
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