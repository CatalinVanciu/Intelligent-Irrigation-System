package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, GeneralView.class);
            startActivity(intent);
        });
    }
}