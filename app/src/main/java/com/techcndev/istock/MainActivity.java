package com.techcndev.istock;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.techcndev.istock.DatabaseHelper.AccountDBHelper;
import com.techcndev.istock.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    final String LOG_TAG = "MainActivity";
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    AccountDBHelper DB;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static final String EXTRA_MESSAGE = "Message key";
    public static final int TEXT_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);
        sharedPreferences = getSharedPreferences("PREFS_DATA", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        DB = new AccountDBHelper(MainActivity.this);
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        String message = intent.getStringExtra(GmailSignUpActivity.EXTRA_MESSAGE);
        Log.d(LOG_TAG, "invokerName: " + message + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, options);

        if (message != null) {
            String dialogText = "Account registered successfully!";
            positive_dialog("SIgn Up", dialogText);
        } else {
            try {
                String user_account = sharedPreferences.getString("current_user",null);
                Log.d(LOG_TAG, "user_account: " + user_account + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                // Check if there is saved account
                if (user_account != null && !user_account.isEmpty()) {
                    update_active_user(user_account,"");
                    launch_inventory();
                } else {
                    Log.d(LOG_TAG, "JUST A NORMAL DAY CAPTAIN!");
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, "Catch Error in MainActivity OnCreate: " + e);
            }
        }
    }

    // Authenticate new account if there is none
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog.isShowing()) {
                        Log.d(LOG_TAG, "result.getResultCode() == RESULT_OK!!!!!!!!!!!!!!!!!!!!! " + result.getResultCode());
                        if (result.getResultCode() == RESULT_OK) {
                            Log.d(LOG_TAG, "result.getResultCode() == RESULT_OK!!!!!!!!!!!!!!!!!!!!! " + result.getResultCode());
                            try {
                                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                                GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                                AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                                auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(LOG_TAG, "task.isSuccessful()!!!!!!!!!!!!!!!!!!!!!");
                                            String email = auth.getCurrentUser().getEmail();
                                            Log.d(LOG_TAG,"email: " + email + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                                            Boolean isExist = DB.checkdata(email);
                                            if(isExist) {
                                                Log.d(LOG_TAG, "if(isExist) !!!!!!!!!!!!!!!!!!!!!");
                                                progressDialog.dismiss();
                                                update_active_user(email,"");
                                                launch_inventory();
                                            } else {
                                                Log.d(LOG_TAG, "NOT task.isSuccessful()!!!!!!!!!!!!!!!!!!!!!");
                                                msign_out();
                                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                                alertDialog.setTitle("Log In Error");
                                                alertDialog.setMessage("Email address is not recognized! Sign up instead?");
                                                alertDialog.setIcon(R.drawable.error_solid);
                                                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        launch_signup(getCurrentFocus());
                                                        dialog.dismiss();
                                                    }
                                                });
                                                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                AlertDialog dialog_create = alertDialog.create();
                                                dialog_create.show();
                                                progressDialog.dismiss();

                                            }
                                        } else {
                                            Log.d(LOG_TAG, "Failed to sign in: ");
                                            msign_out();
                                            Toast.makeText(MainActivity.this, "Failed to sign in: " + task.getException(), Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                });
                            } catch (ApiException e) {
                                Log.d(LOG_TAG, "onActivityResult printStackTrace; >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                                progressDialog.dismiss();
                            }
                        } else {
                            Log.d(LOG_TAG, "result.getResultCode() == NOT RESULT_OK; >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                            Toast.makeText(MainActivity.this, "Failed to sign in", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                }
            }, 2000);
        }
    });

    public void launch_authentication(View view) {
        Log.d(LOG_TAG, "launch_authentication!!!!!!!!!!!!!!!!!!!!!");
        String message = (auth.getCurrentUser() == null) ? "User is not authenticated" : "User is authenticated";
        Log.d(LOG_TAG, message + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        msign_out();
        Intent intent = googleSignInClient.getSignInIntent();
        activityResultLauncher.launch(intent);
    }

    public void launch_signup(View view) {
        Log.d(LOG_TAG, "launch_signup!!!!!!!!!!!!!!!!!!!!!");
        Intent intent = new Intent(MainActivity.this, GmailSignUpActivity.class);
        startActivity(intent);
        finish();
    }

    public void initiate_login(View view) {
        Log.d(LOG_TAG, "initiate_login!!!!!!!!!!!!!!!!!!!!!");
        String email = mainBinding.emailText.getText().toString();
        String password = mainBinding.passText.getText().toString();
        if (!email.isEmpty() && !password.isEmpty()) {
            Cursor res = DB.get_one_data(email);
            if (res.getCount() == 1) {
                while (res.moveToNext()) {
                    String dbEmailPhone = res.getString(0);
                    String dbPass = res.getString(3);
                    if (dbPass != null && !dbPass.isEmpty()) {
                        if (dbPass.equals(password) && email.equals(dbEmailPhone)) {
                            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();
                            update_active_user(dbEmailPhone,"");
                            launch_inventory();
                        } else {
                            negative_dialog("Log In Error", "Password is incorrect!");
                        }
                    } else {
                        negative_dialog("Log In Error", "Account is found! Use Google authenticator instead");
                    }
                }
            } else {
                negative_dialog("Log In Error", "Email address/Phone number is not recognized! Sign up instead");
            }
        } else {
            negative_dialog("Log In Error", "Missing required input!");
        }
    }

    public void launch_inventory() {
        Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
        intent.putExtra(EXTRA_MESSAGE,"SignIn");
        startActivityForResult(intent,TEXT_REQUEST);
        finish();
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

    public void positive_dialog(String title, String text) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
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
