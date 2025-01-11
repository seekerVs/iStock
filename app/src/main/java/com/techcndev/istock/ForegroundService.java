package com.techcndev.istock;

import static com.techcndev.istock.App.CHANNEL_1_ID;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.techcndev.istock.DatabaseHelper.BrandDBHelper;
import com.techcndev.istock.DatabaseHelper.HistoryDBHelper;

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
import java.util.Timer;
import java.util.TimerTask;

public class ForegroundService extends Service {

    Timer timer;
    private static final String LOG_TAG = "ForegroundService";
    private boolean running = false;

    public float ZScore = 1.96f;
    public int LeadTime = 1;
    public float ReorderPoint = 0;
    public String currentDate;
    HistoryDBHelper DB;
    BrandDBHelper DBBrand;
    String currentUser;
    SharedPreferences sharedPreferences;
    private NotificationManagerCompat notificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DB = new HistoryDBHelper(ForegroundService.this);
        DBBrand = new BrandDBHelper(ForegroundService.this);
        sharedPreferences = getSharedPreferences("PREFS_DATA", Context.MODE_PRIVATE);
        currentUser = sharedPreferences.getString("current_user", null);
        currentDate = getCurrentDate();
        timer = new Timer();
        notificationManager = NotificationManagerCompat.from(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!running) {
            running = true;
            createNotificationChannel();
            startForeground(1, buildNotification());
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        initiate_background_check();
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 0, 20000);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void initiate_background_check() throws ParseException {

        double averageDemand = 0;
        double variance = 0;
        double TotalStock = 0;
        double SafetyStock = 0;
        double ReorderPoint = 0;

        List<String> BrandROPList = new ArrayList<>();

        Cursor allBrandCursor = DBBrand.get_all_data();
        List<String> BrandList = new ArrayList<>();
        while(allBrandCursor.moveToNext()) {
            String brandName = allBrandCursor.getString(0);
            BrandList.add(brandName);
        }
        if(!BrandList.isEmpty()) {
            for(String strBrand:BrandList) {
                //  get weekdates of current date
                DateTimeFormatter desiredFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                List<LocalDate> weekDates =  getWeekDates();
                for(LocalDate date:weekDates) {
                    String formattedDate = date.format(desiredFormat);
                    // get the historyid in brand db using currentUser and brandname
                    String hisId = DBBrand.getHistoryId(strBrand,currentUser);
                    // retrieve all the data with the same historyid in history db
                    ArrayList<String[]> historydata = new ArrayList<>();
                    Log.d(LOG_TAG,"String.valueOf(date): " + String.valueOf(date));
                    Log.d(LOG_TAG,"formattedDate: " + formattedDate);
                    Cursor cursor = DB.get_one_data(hisId, formattedDate);
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

                    if(historydata.size() != 0) {
                        averageDemand = calculateAveDemand(historydata);
                        variance = calculatevariance(historydata);
                        TotalStock = totalStock(historydata);
                        SafetyStock = Math.sqrt(LeadTime * variance) * ZScore;
                        ReorderPoint = averageDemand * LeadTime + SafetyStock;
                        if(!Double.isNaN(TotalStock) && !Double.isNaN(ReorderPoint)) {
                            if (TotalStock - ReorderPoint < 0) {
                                if(!BrandROPList.contains(strBrand)) {
                                    BrandROPList.add(strBrand);
                                }
                                Log.d(LOG_TAG, "isSufficient: Insufficient");
                            }
                            Log.d(LOG_TAG, "historydata.size(): " + historydata.size() + " historydata: " + historydata);
                            Log.d(LOG_TAG, "Brand in check: " + strBrand);
                        }
                    }
                    Log.d(LOG_TAG, "historydata.size() != 0: " + (historydata.size() != 0));
                    Log.d(LOG_TAG,"averageDemand: " + averageDemand);
                    Log.d(LOG_TAG,"variance: " + variance);
                    Log.d(LOG_TAG,"TotalStock: " + TotalStock);
                    Log.d(LOG_TAG,"SafetyStock: " + SafetyStock);
                    Log.d(LOG_TAG,"ReorderPoint: " + ReorderPoint);
                    Log.d(LOG_TAG,"isSufficient: Insufficient");

                }
            }
        } else {
            Log.d(LOG_TAG,"No brand found!");
        }
        if(BrandROPList.size() > 0) {
            StringBuilder message = new StringBuilder();
            for(String brand:BrandROPList) {
                message.append(brand).append("\n");
                Log.d(LOG_TAG,"for(String brand:BrandROPList): " + message + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                if (BrandROPList.size() == 1) {
                    sendWarningNotification("Stocks are insufficient for: " + BrandROPList.get(0));
                } else {
                    sendWarningNotification("Stocks are insufficient for: " + BrandROPList.get(0) + "..." + BrandROPList.size()+ "more");
                }

            }
            allBrandCursor.close();
//             String.valueOf(message);
        } else {
            allBrandCursor.close();
            Log.d(LOG_TAG, "All brand have enough stocks...");
//        Str "All brand have enough stocks...";
        }
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
            if (data[3] != null && !data[3].equals("") && !data[3].equals("0") && !data[3].equals("0.0")) {
                sum = sum + Double.parseDouble(data[3]);
            }
        }
        return sum/historydata.size();
    }

    public double calculatevariance(ArrayList<String[]> historydata) {
        double sum = 0;
        ArrayList<Double> dataIntList = new ArrayList<>();
        for (String[] data : historydata) {
            if (data[3] != null && !data[3].equals("") && !data[3].equals("0") && !data[3].equals("0.0")) {
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

    public void sendWarningNotification(String message) {
        Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.warning);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setLargeIcon(img)
                .setSmallIcon(R.mipmap.istock_icon_round)
                .setContentTitle("Warning this week stocks are not sufficient!")
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(2, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "General", "General", NotificationManager.IMPORTANCE_LOW);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "General");
        builder.setSmallIcon(R.mipmap.istock_icon_round);
        builder.setContentTitle("iStock");
        builder.setContentText("Background Service of iStock App is running...");
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return builder.build();
    }

    private void updateNotification(String message) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (notificationManager.areNotificationsEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(1, buildNotification());
        }
    }

    public String getCurrentDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        return formattedDate;
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
}