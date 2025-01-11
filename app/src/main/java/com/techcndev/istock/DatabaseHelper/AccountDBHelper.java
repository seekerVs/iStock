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

public class AccountDBHelper extends SQLiteOpenHelper {

    String LOG_TAG = "AccountHelper";
    Context app_context;
    SharedPreferences sharedPreferences;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    SharedPreferences.Editor editor;

    public AccountDBHelper(Context context) {
        super(context, "iStock.db", null, 2);
        app_context = context;
        sharedPreferences = app_context.getSharedPreferences("PREFS_DATA", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table Accounts(account_user TEXT primary key, " +
                                            "account_email TEXT, account_phone TEXT, " +
                                            "account_password TEXT, account_name TEXT, " +
                                            "account_store TEXT, account_picture BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("drop Table if exists Accounts");
    }


//    public Boolean save_account(String user, String email, String phone,
//                                 String password, String name) {
    public Boolean save_account(String user, String email,
                                String password) {
        SQLiteDatabase DB;
        Cursor cursor;
        ProgressDialog progressDialog = new ProgressDialog(app_context);
        progressDialog.setMessage("Saving account...");
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
            cursor = DB.rawQuery("Select * from Accounts where account_user = ?", new String[]{user});
        } catch (Exception e) {
            Toast.makeText(app_context, "Unable to process: Database initialization error!", Toast.LENGTH_LONG).show();
            return false;
        }
        if(cursor.getCount() == 0) {
            DB = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            if(user != null && !user.isEmpty()) {
                contentValues.put("account_user", user);
                Log.d(LOG_TAG,"user->pass");
            }
            if(email != null || !email.isEmpty()) {
                contentValues.put("account_email", email);
                Log.d(LOG_TAG,"email->pass");
            }
//            if(phone != null && !phone.isEmpty()) {
//                contentValues.put("account_phone", phone);
//                Log.d(LOG_TAG,"phone->pass");
//            }
            if(password != null && !password.isEmpty()) {
                contentValues.put("account_password", password);
                Log.d(LOG_TAG,"password->pass");
            }
//            if(name != null && !name.isEmpty()) {
//                contentValues.put("account_name", name);
//                Log.d(LOG_TAG,"name->pass");
//            }
            long result = DB.insert("Accounts", null, contentValues);

            if (result == -1) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(app_context);
                alertDialog.setTitle("Sign Up");
                alertDialog.setMessage("Account registration failed");
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
//                update_active_user(user,password);
                progressDialog.dismiss();
                cursor.close();
                return true;
            }
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(app_context);
            alertDialog.setTitle("Sign Up");
            alertDialog.setMessage("Account already exists! Sign in instead");
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

    public Boolean update_account(String user, String email, String phone,
                                  String password, String name, String store,
                                  byte[] picture) {
        Log.d(LOG_TAG,"After data DBHELPER: " + " email:" + email + " activeName:" + name + ">>>>>>>>>>>>>>>>>>>>>>>>>");
        ProgressDialog progressDialog = new ProgressDialog(app_context);
        progressDialog.setMessage("Updating account information...");
        progressDialog.show();
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        if (user != null && !user.isEmpty()) {
            contentValues.put("account_user", user);
        }
        if (email != null && !email.isEmpty()) {
            contentValues.put("account_email", email);
        }
        if (phone != null && !phone.isEmpty()) {
            contentValues.put("account_phone", phone);
        }
        if (password != null && !password.isEmpty()) {
            contentValues.put("account_password", password);
        }
        if (name != null && !name.isEmpty()) {
            contentValues.put("account_name", name);
        }
        if (store != null && !store.isEmpty()) {
            contentValues.put("account_store", store);
        }
        if (picture != null && !Arrays.equals(picture, new byte[0])) {
            Log.d(LOG_TAG, "!Arrays.equals(picture, new byte[0]): " + String.valueOf(!Arrays.equals(picture, new byte[0])));
            contentValues.put("account_picture", picture);
        }
        Cursor cursor = DB.rawQuery("Select * from Accounts where account_user = ?", new String[]{user});
        //if has match, update data
        if (cursor.getCount() > 0 || auth.getCurrentUser() != null) {
            long result = DB.update("Accounts", contentValues, "account_user=?", new String[]{user});
            cursor.close();
            update_active_user(user,password);
            if (result == -1) {
                progressDialog.dismiss();
                negative_dialog("","Failed!");
                return false;
            } else {
                progressDialog.dismiss();
//                positive_dialog("","Success");
                return true;
            }
        }
        progressDialog.dismiss();
        return false;
    }

    public Boolean delete_account(String user) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Accounts where account_user = ?", new String[]{user});
        if (cursor.getCount() > 0) {
            long result = DB.delete("Accounts", "account_user=?", new String[]{user});
            if (result == -1) {
                Log.d(LOG_TAG, "Delete Error...");
                return false;
            } else {
                cursor.close();
                return true;
            }
        } else {
            return false;
        }
    }

    public Cursor get_all_data() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Accounts", null);
        return cursor;
    }

    public Cursor get_one_data(String id) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Accounts where account_user = ?", new String[]{id});
        cursor.getCount();
        return cursor;
    }

    //checks if the data is existing using given string
    public Boolean checkdata(String id) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Accounts where account_user = ?", new String[]{id});
        if (cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void update_active_user(String user, String password) {
        // Save in sharedpreferences
        editor.putString("current_user", user);
        editor.putString("current_password", password);
        editor.commit();
    }

    public void positive_dialog(String title, String text) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(app_context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setIcon(R.drawable.success_filled);
//        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                dialog.dismiss();
//            }
//        });
        AlertDialog dialog_create = alertDialog.create();
        dialog_create.show();
    }

    public void negative_dialog(String title, String text) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(app_context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setIcon(R.drawable.error_solid);
//        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                dialog.dismiss();
//            }
//        });
        AlertDialog dialog_create = alertDialog.create();
        dialog_create.show();
    }
}