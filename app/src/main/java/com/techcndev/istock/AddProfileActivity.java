package com.techcndev.istock;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.techcndev.istock.DatabaseHelper.AccountDBHelper;
import com.techcndev.istock.databinding.ActivityAddProfileBinding;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddProfileActivity extends AppCompatActivity {

    final String LOG_TAG = "AddProfileActivity";
    ActivityAddProfileBinding mainBinding;
    FirebaseAuth auth;
    AccountDBHelper DB;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static final int PICK_IMAGE_REQUEST = 99;
    private Uri imagePath;
    private Bitmap imageToStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);
        mainBinding = ActivityAddProfileBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);
        sharedPreferences = getSharedPreferences("PREFS_DATA", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        DB = new AccountDBHelper(AddProfileActivity.this);
        load_components();
    }

    public void load_components() {
        String currentUser = sharedPreferences.getString("current_user", null);
        Log.d(LOG_TAG, LOG_TAG+" currentUser: " + currentUser + ">>>>>>>>>>>>>>>>>>>>>");
        Cursor res = DB.get_one_data(currentUser);

        while (res.moveToNext()) {
            String name = res.getString(4);
            String phone = res.getString(2);
            String email = res.getString(1);
            String storename = res.getString(5);
            byte[] imageData = res.getBlob(6);

            //Assign to its components
            if(name != null && !name.isEmpty()) {
                mainBinding.editTextTextPersonName.setText(name);
            }
            if(phone != null && !phone.isEmpty()) {
                mainBinding.editTextTextPersonName4.setText(phone);
            }
            if(email != null && !email.isEmpty()) {
                mainBinding.editTextTextPersonName5.setText(email);
            }
            if(storename != null && !storename.isEmpty()) {
                mainBinding.editTextTextPersonName6.setText(storename);
            }
            mainBinding.imageView2.setImageResource(R.drawable.profile_circle__1_);
            if(imageData != null && !Arrays.equals(imageData, new byte[0])) {
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                mainBinding.imageView2.setImageBitmap(imageBitmap);
            }
        }
    }

    public void launch_main_profile(View view) {
        Intent intent = new Intent(AddProfileActivity.this,MainProfileActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                imagePath = data.getData();
                imageToStore = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
                mainBinding.imageView2.setImageBitmap(imageToStore);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void launch_inventory(View view) {
        Intent intent = new Intent(AddProfileActivity.this,InventoryActivity.class);
        startActivity(intent);
        finish();
    }

    public void update_profile_data(View view) {
        String name = mainBinding.editTextTextPersonName.getText().toString();
        String phone = mainBinding.editTextTextPersonName4.getText().toString();
        String email = mainBinding.editTextTextPersonName5.getText().toString();
        String storeName = mainBinding.editTextTextPersonName6.getText().toString();
        Bitmap photo = ((BitmapDrawable)mainBinding.imageView2.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bArray = stream.toByteArray();

        if(!isValidPhoneNumber(phone)) {
            mainBinding.editTextTextPersonName4.setText("");
            negative_dialog("Profile Update","Mobile number is invalid. It must contain 11 digits with a prefix of \"09\"");
            return;
        }

        try {
            DB.update_account(email,email,phone,"",name,storeName,bArray);
            Log.d(LOG_TAG, "Account update complete!");
            launch_main_profile(view);
        } catch(Exception e) {
            Toast.makeText(AddProfileActivity.this, "Unable to save your data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        // Define the regular expression pattern
        String regexPattern = "^09\\d{9}$";
        // Create a Pattern object and compile the regex pattern
        Pattern pattern = Pattern.compile(regexPattern);
        // Create a Matcher object to perform the matching
        Matcher matcher = pattern.matcher(phoneNumber);
        // Check if the phone number matches the pattern
        return matcher.matches();
    }

    public void positive_dialog(String title, String text) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddProfileActivity.this);
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

    public void launch_image_picker(View view) {
        try {
            // Instatiate a dialog
            final androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(AddProfileActivity.this);

            // Set a custom layout
            View myview = getLayoutInflater().inflate(R.layout.profile_change_dialog, null);
            alert.setView(myview);

            // Build the custom dialog
            final androidx.appcompat.app.AlertDialog dialog = alert.create();
            dialog.setCancelable(false);

            // Set listener for buttons
            myview.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            myview.findViewById(R.id.choose_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    try {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, PICK_IMAGE_REQUEST);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Display the custom build dialog
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(AddProfileActivity.this, "Dialog Error..." + e, Toast.LENGTH_SHORT).show();
            Log.d("GridAdapter", "GridAdapter: Dialog Error______________ " + e);
        }
    }

    public void negative_dialog(String title, String text) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddProfileActivity.this);
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