package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class ManualPopUp extends AppCompatActivity {
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DatabaseReference databaseReference;
    private TimePicker timePicker;
    private boolean isTimePickerActivated = false;
    private int hourFromTimePicker;
    private int minuteFromTimePicker;
    private String dataWhenPickerIsOn;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_pop_up);
        init();


        timePicker = findViewById(R.id.timePicker);

        Button stopPumpButton = findViewById(R.id.stopPumpButton);
        stopPumpButton.setOnClickListener(view -> stopWaterPump());


        Button startPumpButton = findViewById(R.id.startPumpButton);
        startPumpButton.setOnClickListener(view -> startWaterPump());


    }


    private void init() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = 1500;

        getWindow().setLayout((int) (width*.8), height);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);
    }

    private void startWaterPump(){

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String timeStampToBeAdded = SIMPLE_DATE_FORMAT.format(timestamp);

        databaseReference = FirebaseDatabase.getInstance().getReference();


        if(isTimePickerActivated){
            String currentDate = SIMPLE_DATE_FORMAT.format(timestamp);

            String sysCurrentHour = currentDate.split(" ")[1].split(":")[0];
            String sysCurrentMinute = currentDate.split(" ")[1].split(":")[1];

            Log.d("systest", String.valueOf(sysCurrentHour + " " + sysCurrentMinute));
            Log.d("pickertest", String.valueOf(hourFromTimePicker + " " + minuteFromTimePicker));

            if(Integer.parseInt(sysCurrentHour) >= hourFromTimePicker && Integer.parseInt(sysCurrentMinute) >= minuteFromTimePicker){
                isTimePickerActivated = false;
            }
        }

        if(!isTimePickerActivated){
            databaseReference.child("PumpCommand").child("Status").setValue(1);
            databaseReference.child("PumpCommand").child("Time").setValue(timeStampToBeAdded);
            showToast("Pump has been started");
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void stopWaterPump() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        hourFromTimePicker = timePicker.getHour();
        minuteFromTimePicker = timePicker.getMinute();

        isTimePickerActivated = true;
        databaseReference.child("PumpCommand").child("Status").setValue(0);

        dataWhenPickerIsOn = SIMPLE_DATE_FORMAT.format(timestamp);

        databaseReference.child("PumpCommand").child("Time").setValue(dataWhenPickerIsOn);

        showToast("Pump has been stopped");
    }

    private void showToast(String message){
        Toast.makeText(ManualPopUp.this, message, Toast.LENGTH_SHORT).show();
    }

}