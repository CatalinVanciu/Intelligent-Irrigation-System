package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class RequiredWaterForCrops extends AppCompatActivity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_required_water_for_crops);

        databaseReference = FirebaseDatabase.getInstance().getReference();

    }

    private void readData(IHelper helper){
        ArrayList<Area> areas = new ArrayList<>();
        databaseReference.child("Areas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    Map<String, Object> elements = (Map<String, Object>) snapshot.getValue();
                    assert elements != null;
                    Log.d("TESTTT", String.valueOf(elements.toString()));
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

}