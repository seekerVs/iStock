package com.techcndev.istock.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.techcndev.istock.BrandActivity;
import com.techcndev.istock.DatabaseHelper.BrandDBHelper;
import com.techcndev.istock.DatabaseHelper.HistoryDBHelper;
import com.techcndev.istock.InventoryActivity;
import com.techcndev.istock.R;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class GridAdapter extends BaseAdapter {

    private final String LOG_TAG = "GridAdapter";
    Context context;
    String[] brandName;
    String[] balanceKg;
    private boolean isButtonAdded = false;
    LayoutInflater inflater;
    BrandDBHelper DB;
    HistoryDBHelper DBHistory;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static final String EXTRA_MESSAGE =  "com.techcndev.istock.GridAdapter";
    public static final int TEXT_REQUEST =  3;
    String currentDate;


    public GridAdapter(Context context, String[] brandArr, String[] kgArr) {
        this.context = context;
        this.brandName = brandArr;
        this.balanceKg = kgArr;
        sharedPreferences = context.getSharedPreferences("PREFS_DATA", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        DB = new BrandDBHelper(context);
        DBHistory = new HistoryDBHelper(context);
        currentDate = getCurrentDate();
//        Log.d(LOG_TAG, sharedPreferences.getString("current_user", null) + " ?????????????????????????????????????????????");
    }

    @Override
    public int getCount() {
        return isButtonAdded ? brandName.length + 1 : balanceKg.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void addGridButton() {
        if (!isButtonAdded) {
            isButtonAdded = true;
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        long count = Arrays.stream(brandName).count();
            if (inflater == null) {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            if (isButtonAdded && position == getCount() - 1) {
                convertView = inflater.inflate(R.layout.grid_button, null);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Toast.makeText(context, "Launching Stock Adder...", Toast.LENGTH_SHORT).show();
                        show_stock_adder();
                    }
                });
            } else {
                String  currentUser = sharedPreferences.getString("current_user", null);
                String historyId = DB.getHistoryId(brandName[position],currentUser);
                convertView = inflater.inflate(R.layout.grid_item, null);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Toast.makeText(context,"GridView item: " + brandName[position] + " was clicked", Toast.LENGTH_SHORT).show();
                        launch_brand_activity(brandName[position], historyId);
                    }
                });
                convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);
                        alertDialog.setTitle("Delete Brand");
                        alertDialog.setMessage("Are your sure you want to delete \"" + brandName[position] +"\" as a brand?");
                        alertDialog.setIcon(R.drawable.success_filled);
                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d(LOG_TAG, brandName[position] + " <<<<<>>>>>>>> " +currentUser);
                                boolean IsDeleted = DB.delete_brand(brandName[position],currentUser);
                                if(IsDeleted) {
                                    positive_dialog("Brand Delete", "Deletion successful!");
                                }
                                Intent intent = new Intent(context, InventoryActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                context.startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        android.app.AlertDialog dialog_create = alertDialog.create();
                        dialog_create.show();
                        return false;
                    }
                });

                TextView brand = convertView.findViewById(R.id.brand_textview);
                TextView kg = convertView.findViewById(R.id.kg_textview);

                brand.setText(brandName[position]);
                kg.setText(balanceKg[position]);
            }

        return convertView;
    }

    public void show_stock_adder() {
        try {
            // Instantiate a dialog
            final AlertDialog.Builder alert = new AlertDialog.Builder(context);

            // Set a custom layout
            View myview = LayoutInflater.from(context).inflate(R.layout.activity_add_stock, null);
            alert.setView(myview);

            // Get a reference for some components
            TextView unit1TextView, unit2TextView, inputTextView, value1TextView, value2TextView, totalQtyTextView;
            unit1TextView = myview.findViewById(R.id.kg_text);
            unit2TextView = myview.findViewById(R.id.unit_text);
            inputTextView = myview.findViewById(R.id.input_view);
            value1TextView = myview.findViewById(R.id.conver_view1);
            value2TextView = myview.findViewById(R.id.conver_view2);
            totalQtyTextView = myview.findViewById(R.id.total_qty_view);
            Spinner unitSpinner = myview.findViewById(R.id.unit_spinner);

            // Build the custom dialog
            final AlertDialog dialog = alert.create();
            dialog.setCancelable(false);

            // Initialize components attributes
            String[] arraySpinner = new String[] { "Kg", "Sack" };

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(myview.getContext(),
                    android.R.layout.simple_spinner_item, arraySpinner);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unitSpinner.setAdapter(adapter);

            // Set listener for buttons
            myview.findViewById(R.id.cancel_add_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            myview.findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String str = inputTextView.getText().toString();
                    int current_count = Integer.parseInt(str);
                    current_count++;
                    inputTextView.setText(String.valueOf(current_count));
                    value1TextView.setText(String.valueOf(current_count));
                    int currValue = Integer.parseInt(value1TextView.getText().toString());
                    String currUnit = unitSpinner.getSelectedItem().toString();
                    float result =  compute_unit_value(currUnit,currValue);
                    value2TextView.setText((String.valueOf(result)));
                    totalQtyTextView.setText((String.valueOf(result)));
//                    Toast.makeText(context, "onClick: " + result, Toast.LENGTH_SHORT).show();

                }
            });

            myview.findViewById(R.id.minus_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String str = inputTextView.getText().toString();
                    int current_count = Integer.parseInt(str);
                    if(current_count > 0) {
                        current_count--;
                        inputTextView.setText(String.valueOf(current_count));
                        value1TextView.setText(String.valueOf(current_count));
                        int currValue = Integer.parseInt(value1TextView.getText().toString());
                        String currUnit = unitSpinner.getSelectedItem().toString();
                        float result =  compute_unit_value(currUnit,currValue);
                        value2TextView.setText((String.valueOf(result)));
                        totalQtyTextView.setText((String.valueOf(result)));
//                        Toast.makeText(context, "onClick: " + result, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ((Spinner) myview.findViewById(R.id.unit_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String spinner_label = parent.getItemAtPosition(position).toString();
//                    Toast.makeText(context, "You've clicked: " + spinner_label, Toast.LENGTH_SHORT).show();
                    if(spinner_label.equals("Kg")) {
                        unit1TextView.setText("Kilo");
                        unit2TextView.setText("Sack");
                        int currValue = Integer.parseInt(value1TextView.getText().toString());
                        String currUnit = unitSpinner.getSelectedItem().toString();
                        float result =  compute_unit_value(currUnit,currValue);
                        value2TextView.setText((String.valueOf(result)));
                        totalQtyTextView.setText((String.valueOf(result)));
//                        Toast.makeText(context, "onClick: " + result, Toast.LENGTH_SHORT).show();

                    } else {
                        unit1TextView.setText("Sack");
                        unit2TextView.setText("Kilo");
                        int currValue = Integer.parseInt(value1TextView.getText().toString());
                        String currUnit = unitSpinner.getSelectedItem().toString();
                        float result =  compute_unit_value(currUnit,currValue);
                        value2TextView.setText((String.valueOf(result)));
                        totalQtyTextView.setText((String.valueOf(result)));
//                        Toast.makeText(context, "onClick: " + result, Toast.LENGTH_SHORT).show();
                    }

                    myview.findViewById(R.id.add_list_btn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText brandNameEditText = myview.findViewById(R.id.editText);
                            EditText totalQuantityEditText = myview.findViewById(R.id.totalquantity);
                            EditText priceEditText = myview.findViewById(R.id.price);
                            String brandName = brandNameEditText.getText().toString();
                            String totalQuantity = totalQuantityEditText.getText().toString();
                            String price = priceEditText.getText().toString();
                            String current_user = sharedPreferences.getString("current_user",null);
                            Log.d(LOG_TAG, LOG_TAG + " current_user" + current_user + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                            int val = generate_history_id(4);
                            String history_id = String.valueOf(val);

                            if(brandName.isEmpty() && totalQuantity.isEmpty() && price.isEmpty()) {
                                negative_dialog("Brand Adding", "Error: Missing required inputs!");
                                return;
                            }

                            if(brandName.isEmpty()) {
                                negative_dialog("Brand Adding", "Error: Missing brand name!");
                                brandNameEditText.setText("");
                                return;
                            } else {
                                brandName = capitalizeWord(brandName);
                            }
                            String totalQty = "";
                            if(!totalQuantity.isEmpty()) {
                                totalQuantity = capitalizeWord(totalQuantityEditText.getText().toString());
                                Log.d(LOG_TAG, LOG_TAG + " totalQuantity: " + totalQuantity + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + " !totalQuantity.endsWith(Kilo): " + !totalQuantity.endsWith(" Kilo"));
                                if (totalQuantity.endsWith(" Kilo") || totalQuantity.endsWith(" Sack")) {
                                    char breakpoint = ' ';
                                    for(char charStr:totalQuantity.toCharArray()) {
                                        if(charStr != breakpoint) {
                                            totalQty = totalQty + charStr;
                                        } else {
                                            break;
                                        }
                                    }
                                    if(!isDigit(totalQty)) {
                                        negative_dialog("Brand Adding", "Error: Quantity must only contain digits!");
                                        totalQuantityEditText.setText("");
                                        return;
                                    } else {
                                        // convert the quantity to kilo if it is in sack
                                        if(totalQuantity.endsWith(" Sack")) {
                                            float quantityFloat = Integer.parseInt(totalQty);
                                            float convertVal = quantityFloat * 50.0f;
                                            totalQty = String.valueOf(convertVal);
                                        } else {
                                            float quantityFloat = Integer.parseInt(totalQty);
                                            totalQty = String.valueOf(quantityFloat);
                                        }
                                    }
                                } else {
                                        negative_dialog("Brand Adding", "Error: Input quantity is in the wrong format! Follow this case-insensitive example \"20 Kilo or 20 Sack\"");
                                        totalQuantityEditText.setText("");
                                        return;
                                }
                            } else {
                                    negative_dialog("Brand Adding", "Error: Missing stock quantity name!");
                                    totalQuantityEditText.setText("");
                                    return;
                                }
                            if(!price.isEmpty()) {
                                if(isDigit(price)) {
                                } else {
                                    negative_dialog("Brand Adding", "Error: Price must only contain digits!");
                                    priceEditText.setText("");
                                    return;
                                }
                            } else {
                                negative_dialog("Brand Adding", "Error: Missing brand price name!");
                                priceEditText.setText("");
                                return;
                            }

                            boolean isSaved = DB.save_brand(brandName,history_id,current_user,price,totalQty);
                            DBHistory.create_history(history_id, currentDate,totalQty,"", totalQty,"");

                            if(isSaved) {
                                Intent intent = new Intent(context, InventoryActivity.class);
                                dialog.dismiss();
//                                adapter.notifyDataSetChanged();
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                context.startActivity(intent);
//                                finish();
                            }
                        }
                    });
                }



                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            // Display the custom build dialog
            dialog.show();
        } catch (Exception e) {
//            Toast.makeText(context.getApplicationContext(), "Dialog Error..." + e, Toast.LENGTH_SHORT).show();
            Log.d("GridAdapter","GridAdapter: Dialog Error______________ "+ e);
        }
    }

    public String getCurrentDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        return formattedDate;
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

    public boolean isDigit(String strDigits) {
        for(char charStr:strDigits.toCharArray()) {
            if(!Character.isDigit(charStr)) {
                return false;
            }
        }
        return true;
    }

    private float compute_unit_value(String current_unit,int current_value) {
        float result;
        // Compute the data based on the unit
        if(current_unit.equals("Kg")) {
            result = current_value/50.0f;
        } else {
            result =  current_value * 50.0f;
        }
        Log.d(LOG_TAG, "result: " + result + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        return result;
    }

    public int generate_history_id(int length) {
        while(true) {
            String num = "";
            Random rand = new Random();
            int counter = length;
            while(counter > 0) {
                num = num + String.valueOf(rand.nextInt(9));
                counter--;
            }
            boolean isUsed = DB.check_history_id(num);
            if(!isUsed) {
                Log.d(LOG_TAG, "generate_history_id: " +  Integer.parseInt(num) + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                return Integer.parseInt(num);
            }
        }
    }

    public void launch_brand_activity(String brandName,String user) {
        String[] message = {brandName,user};
        Intent intent = new Intent(context, BrandActivity.class);
        intent.putExtra(EXTRA_MESSAGE,message);
        ((Activity) context).startActivityForResult(intent, TEXT_REQUEST);
    }

    public void positive_dialog(String title, String text) {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setIcon(R.drawable.success_filled);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        android.app.AlertDialog dialog_create = alertDialog.create();
        dialog_create.show();
    }

    public void negative_dialog(String title, String text) {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setIcon(R.drawable.error_solid);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        android.app.AlertDialog dialog_create = alertDialog.create();
        dialog_create.show();
    }

}
