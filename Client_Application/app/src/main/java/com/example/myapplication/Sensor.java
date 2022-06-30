package com.example.myapplication;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Sensor {

    private static final int MAX_PROPER_VALUE_FOR_SUNFLOWER = 300;
    private static final int MAX_PROPER_VALUE_FOR_OTHERS = 800;

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

        if(crop.equalsIgnoreCase("Sunflower")){
            if(data < waterRequired){
                state = 1;
            } else if(data >= waterRequired || data >= MAX_PROPER_VALUE_FOR_SUNFLOWER){
                state = 0;
            }
        } else{
            if(data < waterRequired){
                //modify waterpump state to 1
                state = 1;
            } else if(data >= waterRequired || data >= MAX_PROPER_VALUE_FOR_OTHERS){
                //modify waterpump state to 0
                state = 0;
            }
        }

            databaseReference.child("PumpCommand").child("Status").setValue(state);
    }

    public int getHumidityPercent(double waterRequired){
        int result = 0;
        if(data >= waterRequired){
            return 100;
        } else {
            result = (int) ((int) data/waterRequired * 100);
        }

        return result;
    }

    public int getUVPercent(){

        int result = 0;

        if (data != 0) {
            result = (int) (data / 3 * 100);
        }

        return result;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
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
