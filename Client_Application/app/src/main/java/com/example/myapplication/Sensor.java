package com.example.myapplication;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Sensor {
    private String type;
    private String date;
    private double data;

    public Sensor(){

    }

    public Sensor(String type, String date, double data){
        this.type = type;
        this.date = date;
        this.data = data;
    }

    public void compareWithProperValue(double waterRequired, String crop){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        int state = 0;

        if(type.equalsIgnoreCase("HumiditySensor")){

            if(crop.equalsIgnoreCase("Sunflower")){
                if(data < waterRequired){
                    state = 1;
                } else if(data >= waterRequired || data >= 300){
                    state = 0;
                }
                Log.d("SUNFLOWER", String.valueOf(crop + " " + data));
            } else{
                if(data < waterRequired){
                    //modify waterpump state to 1
                    state = 1;
                } else if(data >= waterRequired || data >= 800){
                    //modify waterpump state to 0
                    state = 0;
                }
                Log.d("CROP IF", String.valueOf(crop + " " + data));
            }

            databaseReference.child("PumpCommand").child("Status").setValue(state);

        }
    }

    public int getHumidityPercent(double waterRequired){
        int result = 0;

        if(type.equalsIgnoreCase("HumiditySensor")){
            if(data >= waterRequired){
                result = 100;
            } else {
                result = (int) ((int) data/waterRequired * 100);
            }
        }

        if(result > 100){
            return 100;
        }

        return result;
    }


    public String getDate(){
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public double getData(){
        return data;
    }

    public void setData(double data){
        this.data = data;
    }

    public String toString(){
        return type + " - " + date + " - " + data;
    }

}
