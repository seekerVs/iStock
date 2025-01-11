package com.techcndev.istock;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.techcndev.istock.DatabaseHelper.AccountDBHelper;
import com.techcndev.istock.databinding.ActivityMainProfileBinding;

import java.util.Arrays;

public class MainProfileActivity extends AppCompatActivity {

    final String LOG_TAG = "MainProfileActivity";
    ActivityMainProfileBinding mainBinding;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    AccountDBHelper DB;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String name, email, phone, storename, pass;
    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_profile);
        mainBinding = ActivityMainProfileBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);
        sharedPreferences = getSharedPreferences("PREFS_DATA", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        auth = FirebaseAuth.getInstance();
        DB = new AccountDBHelper(MainProfileActivity.this);
        // Insert account data in logged account
        load_components();

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(MainProfileActivity.this, options);
    }

    private void load_components() {
        // Get the data from Account DB

        String currentUser = sharedPreferences.getString("current_user", null);

        Cursor res = DB.get_one_data(currentUser);

        String status = sharedPreferences.getString("is_notif_enabled",null);
        if(status == null) {
            editor.putString("is_notif_enabled", "true");
            editor.commit();
            mainBinding.notifButton.setImageResource(R.drawable.notification_on_outline);
        } else if (status.equals("false")){
            mainBinding.notifButton.setImageResource(R.drawable.notification_off_solid);
        } else {
            mainBinding.notifButton.setImageResource(R.drawable.notification_on_outline);
        }

        while (res.moveToNext()) {
            name = res.getString(4);
            email = res.getString(1);
            phone = res.getString(2);
            storename = res.getString(5);
            pass = res.getString(3);
            byte[] imageData = res.getBlob(6);

            Log.d(LOG_TAG, "name: "+name + " email: "+email + " phone: " + phone + " storename: " + storename + " pass: "+ pass + ">>>>>>>>>>>>>>>>>>>>>");

            //Assign to its components
            if(name != null) {
                mainBinding.nameTextview.setText(name);
            }
            if(phone != null) {
                mainBinding.yourmobileTextView.setText(phone);
            }
            if(email != null) {
                mainBinding.emailAddtextView.setText(email);
            }
            if(storename != null) {
                mainBinding.namestoreTextview.setText(storename);
            }
            if(imageData != null && !Arrays.equals(imageData, new byte[0])) {
                imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                mainBinding.imageView2.setImageBitmap(imageBitmap);
            }
        }
    }

    public void launch_add_profile(View view) {
        Intent intent = new Intent(MainProfileActivity.this,AddProfileActivity.class);
        startActivity(intent);
    }

    public void launch_inventory(View view) {
        Intent intent = new Intent(MainProfileActivity.this,InventoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void start_logout(View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainProfileActivity.this);
        alertDialog.setTitle("Log Out");
        alertDialog.setMessage("Are you sure you want to logout?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                editor.putString("current_user", "");
                editor.putString("current_password", "");
                editor.commit();
                Log.d(LOG_TAG,"start_logout started >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                if(auth.getCurrentUser() != null) {
//                    auth.signOut();
                    Log.d(LOG_TAG, MainProfileActivity.class.getSimpleName() + " There is a user before sign-out>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                }
                dialog.dismiss();
                msign_out();
                Intent intent = new Intent(MainProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
            AlertDialog dialog_create = alertDialog.create();
            dialog_create.show();
    }

    private void startCounterService() {
        Context context = getApplicationContext();
        Intent intent = new Intent(this, ForegroundService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private void stopCounterService() {
        Context context = getApplicationContext();
        Intent intent = new Intent(this, ForegroundService.class);
        context.stopService(intent);
    }


    private void update_active_user(String user, String password) {
        // Save in sharedpreferences
        editor.putString("current_user", user);
        editor.commit();
    }

    public void msign_out() {
        if(auth.getCurrentUser() != null) {
            auth.signOut();
            googleSignInClient.signOut();
            update_active_user("","");
        }
    }

    public void disable_notification(View view) {
        String status = sharedPreferences.getString("is_notif_enabled",null);
        Log.d(LOG_TAG,"disable_notification is_notif_enabled START: " + status + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Log.d(LOG_TAG,"disable_notification is_notif_enabled: " + status + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        if(status.equals("true")) {
            mainBinding.notifButton.setImageResource(R.drawable.notification_off_solid);
            stopCounterService();
            editor.putString("is_notif_enabled", "false");
            editor.commit();
        } else {
            mainBinding.notifButton.setImageResource(R.drawable.notification_on_outline);
            startCounterService();
            editor.putString("is_notif_enabled", "true");
            editor.commit();
        }
        Log.d(LOG_TAG,"disable_notification is_notif_enabled END: " + status + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
}