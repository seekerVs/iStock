package com.techcndev.istock;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.techcndev.istock.databinding.ActivityGmailSignUpBinding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GmailSignUpActivity extends AppCompatActivity {

    ActivityGmailSignUpBinding mainBinding;
    final String LOG_TAG = "GmailSignUpActivity";
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    AccountDBHelper DB;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static final String EXTRA_MESSAGE = "Message key";
    public static final int TEXT_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmail_sign_up);
        mainBinding = ActivityGmailSignUpBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);
        sharedPreferences = getSharedPreferences("PREFS_DATA", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        auth = FirebaseAuth.getInstance();
        DB = new AccountDBHelper(GmailSignUpActivity.this);
        out_google_auth();

        mainBinding.imageGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launch_authentication();
            }
        });

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(GmailSignUpActivity.this, options);

    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                    AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                    auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String email = auth.getCurrentUser().getEmail();
                                String displayName = auth.getCurrentUser().getDisplayName();
                                // save data in db
                                save_data(email,displayName);
                            } else {
                                Toast.makeText(GmailSignUpActivity.this, "Failed to sign in: " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    });


    public void save_data(String email, String activeName) {
        ProgressDialog progressDialog = new ProgressDialog(GmailSignUpActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
            if (progressDialog != null && progressDialog.isShowing()) {
                boolean isAccountExist = DB.checkdata(email);
                Boolean isSaved = null;
                Log.d(LOG_TAG, "Before data: " + " email:" + email + " activeName:" + activeName + ">>>>>>>>>>>>>>>>>>>>>>>>>");
                if (isAccountExist) {
                    byte[] mbyte = new byte[0];
                    Toast.makeText(GmailSignUpActivity.this, "Account detected! Signing in... ", Toast.LENGTH_SHORT).show();
                    isSaved = DB.update_account(email, email, "", "", "", "", mbyte);
                } else {
                    editor.putString("current_user", email);
                    editor.commit();
                    isSaved = DB.save_account(email, email, "");
                }

                Log.d(LOG_TAG, "Data savedd: " + String.valueOf(isSaved) + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                if (isSaved) {
//                        Toast.makeText(GmailSignUpActivity.this, "Signed in successfully!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    Log.d(LOG_TAG, "Data savedd: " + String.valueOf(isSaved) + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    launch_inventory();
                } else {
//                        Toast.makeText(GmailSignUpActivity.this, "email: " + email, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
    }

    public void start_signup(View view) {
        // When sign up button is clicked
        // Display Log in in progress dialog
        if(!mainBinding.AgreementCheckBox.isChecked()) {
            Toast.makeText(GmailSignUpActivity.this, "Agree with terms and conditions by clicking the checkbox to proceed",Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog progressDialog = new ProgressDialog(GmailSignUpActivity.this);
        progressDialog.setMessage("Sign up processing...");
        progressDialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    // Get email,password,password confirmation
                    String email = mainBinding.emailText.getText().toString();
                    String password = mainBinding.passText.getText().toString();
                    String confirmPassword = mainBinding.confirmPassText.getText().toString();
                    // Check if the email is already used
                    String user_key = email;
                    boolean isExist = DB.checkdata(user_key);
                    // If email is not used
                    Log.d(LOG_TAG, "start_signup user_key: " + user_key + "isExist: " + isExist + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    if (!isExist) {
                        // Check if password and confirmation is the same
                        if (!password.isEmpty() || !confirmPassword.isEmpty() || !email.isEmpty()) {
                            if (!isValidEmail(email)) {
                                progressDialog.dismiss();
                                run_alert_dialog("Sign Up", "Invalid email address!");
                                return;
                            }
                            // Check if the password contains 6 characters
                            if (password.length() != 6 || confirmPassword.length() != 6) {
                                progressDialog.dismiss();
                                run_alert_dialog("Sign Up", "Invalid password! It must have 6 characters!");
                                return;
                            }
                            if (password.equals(confirmPassword)) {
                                // If the same
                                // Save in Account DB the account_name,email,password
                                boolean isSaved = DB.save_account(email, email, password);
                                if (isSaved) {
                                    progressDialog.dismiss();
                                    // Open the sign in
                                    Intent intent = new Intent(GmailSignUpActivity.this, MainActivity.class);
                                    intent.putExtra(EXTRA_MESSAGE, "LogIn");
                                    startActivityForResult(intent,TEXT_REQUEST);
                                    finish();

                                }
                            } else {
                                // If confirmation is not the same
                                // Display "not matched" error
                                progressDialog.dismiss();
                                run_alert_dialog("Sign Up", "Password and confirmation password do not match. Please ensure they are the same");
                            }
                        } else {
                            // If there is missing input
                            progressDialog.dismiss();
                            run_alert_dialog("Sign Up", "Missing required input! Try again");
                        }
                    } else {
                        // If email is used
                        // Cancel sign up
                        // Display "user exists" error
                        progressDialog.dismiss();
                        run_alert_dialog("Sign Up", "Account already exists!");
                    }

                }
            }
        }, 2000);
    }

    public boolean isValidEmail(String email) {
        // Regular expression for a valid email address
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        // Compile the regex pattern
        Pattern pattern = Pattern.compile(regex);
        // Match the input email against the pattern
        Matcher matcher = pattern.matcher(email);
        // Return true if the email matches the pattern
        return matcher.matches();
    }

    public void run_alert_dialog(String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GmailSignUpActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.error_solid);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog_create = alertDialog.create();
        dialog_create.show();
    }

    public void launch_inventory() {
        Intent intent = new Intent(GmailSignUpActivity.this, InventoryActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "Registration");
        startActivity(intent);
        finish();
    }

    public void lauch_signin(View view) {
        Intent intent = new Intent(GmailSignUpActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void update_active_user(String user, String password) {
        // Save in sharedpreferences
        editor.putString("current_user", user);
        editor.putString("current_password", password);
        editor.commit();
    }

    public void out_google_auth() {
        if (auth.getCurrentUser() != null) {
            auth.signOut();
            googleSignInClient.signOut();
            update_active_user("", "");
            Log.d(LOG_TAG, "Google authentication account detected: Signing out while initializing" + this.getLocalClassName());
        }
    }

    public void launch_authentication() {
        if(!mainBinding.AgreementCheckBox.isChecked()) {
            negative_dialog("Sign_up", "Account is not registered. Agree with terms and conditions by checking the checkbox to proceed");
            return;
        }
        FirebaseApp.initializeApp(this);

        auth = FirebaseAuth.getInstance();
        // Check if there is saved account
        String message = (auth.getCurrentUser() == null) ? "User is not authenticated" : "User is authenticated";
        Log.d(LOG_TAG,message);
        Intent intent = googleSignInClient.getSignInIntent();
        activityResultLauncher.launch(intent);
    }

    public void positive_dialog(String title, String text) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GmailSignUpActivity.this);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GmailSignUpActivity.this);
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

    public void launch_agreement(View view) {
        Intent intent = new Intent(GmailSignUpActivity.this, AgreementActivity.class);
        startActivity(intent);
    }
}