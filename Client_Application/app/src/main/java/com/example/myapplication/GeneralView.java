package com.example.myapplication;

import static java.lang.Double.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GeneralView extends AppCompatActivity {


    private final Handler HANDLER = new Handler();
    private Runnable runnable;
    private static final int DELAY = 10000; //10 seconds
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_view);
        createNotificationChannel();

        Button requiredWaterButton = findViewById(R.id.requiredWaterButton);
        requiredWaterButton.setOnClickListener(view -> openActivity(RequiredWaterForCrops.class));

        Button manualWatering = findViewById(R.id.manualButton);
        manualWatering.setOnClickListener(view -> {
        });

        Intent intent = getIntent();
        String area = intent.getStringExtra("area");
        String crop = intent.getStringExtra("crop");

        databaseReference = FirebaseDatabase.getInstance().getReference();

        retrieveWaterRequiredFromFirebase(area, crop);

        //retrieveSensorDataFromFirebase(databaseReference);

//        TextView viewWaterProgressBar = findViewById(R.id.viewWaterProgressBar);
//        TextView viewHumidityProgressBar = findViewById(R.id.viewHumidityProgressBar);
//        TextView viewLightProgressBar = findViewById(R.id.viewLightProgressBar);

        addNotification();
//        addDelayPopUp();
//        pauseDelay();
    }


    private void openActivity(final Class<? extends Activity> activityToOpen) {
        Intent intent = new Intent(this, activityToOpen);
        startActivity(intent);
    }

    private void addNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel1")
                .setSmallIcon(R.drawable.ic_sharp_notification_important_24).setContentTitle("Content Title")
                .setContentText("Content Text");

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(100, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel";
            String description = "Channel for crops";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel1", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void addDelayPopUp(){
        HANDLER.postDelayed(runnable = () -> {
            HANDLER.postDelayed(runnable, DELAY);
            Toast.makeText(GeneralView.this, "This method is run every 10 seconds",
                    Toast.LENGTH_SHORT).show();
            // addNotification();
        }, DELAY);
        super.onResume();
    }

    private void pauseDelay() {
        HANDLER.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
    }

    private void retrieveWaterRequiredFromFirebase(String area, String crop){
        databaseReference.child("Areas").child(area).child(crop).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Log.d("snapshot", String.valueOf(snapshot.getValue() + " - " + snapshot.getValue().getClass()));

                    double waterRequired = (Long) snapshot.getValue();

                    retrieveSensorDataFromFirebase(waterRequired, crop);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void retrieveSensorDataFromFirebase(double waterRequired, String crop){
        Runnable thread = () -> {
            Query query = databaseReference.child("Sensors").limitToLast(3).orderByValue();
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<Sensor> sensors = getArrayOfSensors(snapshot);

                    Log.d("SENORS", sensors.toString());

                    for(Sensor sensor : sensors){
                        sensor.compareWithProperValue(waterRequired, crop);
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        };

        scheduler.scheduleWithFixedDelay(thread, 10, 10, TimeUnit.SECONDS);
    }

    private ArrayList<Sensor> getArrayOfSensors(@NonNull DataSnapshot snapshot) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        ArrayList<Date> dates = new ArrayList<>();
        ArrayList<Sensor> result = new ArrayList<>();
        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
            Map<String, Double> mp = (Map<String, Double>) dataSnapshot.getValue();
            for(Map.Entry<String, Double> elem : mp.entrySet()){
                try{
                       Date date = sdf.parse(elem.getKey());
                       dates.add(date);
                } catch(ParseException e){
                    e.printStackTrace();
                }
            }
            Collections.sort(dates);

            String finalDate = sdf.format(dates.get(dates.size() - 1));
            double sensorData = 0;

            for(Map.Entry<String, Double> elem : mp.entrySet()){
                if(finalDate.equalsIgnoreCase(elem.getKey())){
                    //Log.d("SENSOR", String.valueOf(elem.getValue()));
                    sensorData = parseDouble(String.valueOf(elem.getValue()));
                    break;
                }
            }

           // Log.d("SENSORS", String.valueOf(dataSnapshot.getKey() + " - " + finalDate + " - " + sensorData));
            Sensor sensor = new Sensor(dataSnapshot.getKey(), finalDate, sensorData);
            result.add(sensor);
        }

        return result;

    }


}