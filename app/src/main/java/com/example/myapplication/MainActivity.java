package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(view -> showDialog());

    }

    private void openActivity() {
        Intent intent = new Intent(this, GeneralView.class);
        startActivity(intent);
    }

    private void showDialog(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);

        //Setting message manually and performing action on button click
        builder.setMessage("Are you sure?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> openActivity())
                .setNegativeButton("No", (dialog, id) -> {
                    //  Action for 'No' Button
                    // Toast.makeText(getApplicationContext(),"No",Toast.LENGTH_SHORT).show();
                });
        //Creating dialog box

        AlertDialog alert = builder.create();
        alert.show();
    }
}