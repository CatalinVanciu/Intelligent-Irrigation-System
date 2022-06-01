package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Spinner chooseAreaSpinner;
    private Spinner chooseCropSpinner;
    private DatabaseReference databaseReference;
    private ArrayList<Area> areas;
    RetrieveDataFromFirebaseHelper retrieveDataFromFirebaseHelper = RetrieveDataFromFirebaseHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(view -> showDialog());

        databaseReference = FirebaseDatabase.getInstance().getReference();

        chooseAreaSpinner = findViewById(R.id.chooseAreaSpinner);
        chooseCropSpinner = findViewById(R.id.chooseCropSpinner);

        readData(areasReceived -> {
//            for(Area a : areasReceived){
//                Log.d("TEST", String.valueOf(a.getName()));
//            }
            ArrayList<String> areasNames = getAreasNames(areasReceived);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, areasNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            chooseAreaSpinner.setAdapter(adapter);
            chooseAreaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    ArrayList<String> cropNames = getCropNames(areasReceived, (String) chooseAreaSpinner.getSelectedItem());
                    ArrayAdapter<String> cropAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, cropNames);
                    cropAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    chooseCropSpinner.setAdapter(cropAdapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        });

    }

    private ArrayList<String> getCropNames(ArrayList<Area> areasReceived, String areaSelected){
        ArrayList<String> cropNames = new ArrayList<>();
        
        for(Area a : areasReceived){
            if(a.getName().equalsIgnoreCase(areaSelected)){
                for(Crop crop : a.getCrops()){
                    cropNames.add(crop.getCropName());
                }
            }
        }
        
        return cropNames;
    }
    
    private ArrayList<String> getAreasNames(ArrayList<Area> areasReceived) {
        ArrayList<String> areasSpinner = new ArrayList<>();
        for(Area a : areasReceived){
            areasSpinner.add(a.getName());
        }

        return areasSpinner;
    }

    @NonNull
    private void readData(IHelper helper){
        ArrayList<Area> areas = new ArrayList<>();
        databaseReference.child("Areas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    Map<String, Object> elements = (Map<String, Object>) snapshot.getValue();
                    assert elements != null;
                    for(Map.Entry<String, Object> elem : elements.entrySet()){
                        Map<String, Double> crops = (Map<String, Double>) elem.getValue();
                        ArrayList<Crop> cropsToBeAdded = new ArrayList<>();
                        for(Map.Entry<String, Double> cropElem : crops.entrySet()){
                            // Log.d("TEST IN FOR: ", String.valueOf(cropElem.getKey() + " " + cropElem.getValue()));
                            Crop crop = new Crop(cropElem.getKey(), String.valueOf(cropElem.getValue()));
                            cropsToBeAdded.add(crop);
                        }

                        Area area = new Area(elem.getKey(), cropsToBeAdded);

                        areas.add(area);
                    }
                }
                helper.areasReceived(areas);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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