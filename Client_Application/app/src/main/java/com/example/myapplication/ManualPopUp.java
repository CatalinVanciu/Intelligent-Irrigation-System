package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class ManualPopUp extends AppCompatActivity {

    private Button startPumpButton;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_pop_up);
        init();

        startPumpButton = findViewById(R.id.startPumpButton);
        startPumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWaterPump();
            }
        });

    }

    private void init() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = 1000;

        getWindow().setLayout((int) (width*.8), height);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);
    }

    private void startWaterPump(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String timeStampToBeAdded = sdf.format(timestamp);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("PumpCommand").child("Status").setValue(1);
        databaseReference.child("PumpCommand").child("Time").setValue(timeStampToBeAdded);
        Toast.makeText(ManualPopUp.this, "Pump has been started", Toast.LENGTH_SHORT).show();

    }
}