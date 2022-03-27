package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class MainActivity extends AppCompatActivity {

    private final Handler HANDLER = new Handler();
    private Runnable runnable;
    private static final int DELAY = 10000; //10 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        Button requiredWaterButton = findViewById(R.id.requiredWaterButton);
        requiredWaterButton.setOnClickListener(view -> openRequiredWaterForCrops());

        TextView viewWaterProgressBar = findViewById(R.id.viewWaterProgressBar);
        TextView viewHumidityProgressBar = findViewById(R.id.viewHumidityProgressBar);
        TextView viewLightProgressBar = findViewById(R.id.viewLightProgressBar);

        addNotification();
//        addDelayPopUp();
//        pauseDelay();
    }

    private void openRequiredWaterForCrops() {
        Intent intent = new Intent(this, RequiredWaterForCrops.class);
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
            CharSequence name = "studentChannel";
            String description = "Channel for students notificaton";
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
            Toast.makeText(MainActivity.this, "This method is run every 10 seconds",
                    Toast.LENGTH_SHORT).show();
            // addNotification();
        }, DELAY);
        super.onResume();
    }

    private void pauseDelay() {
        HANDLER.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
    }


}