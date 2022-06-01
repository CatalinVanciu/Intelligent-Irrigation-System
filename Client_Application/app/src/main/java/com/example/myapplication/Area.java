package com.example.myapplication;

import java.util.ArrayList;

public class Area {

    private String name;
    private ArrayList<Crop> crops;

    public Area(){

    }

    public Area(String name){
        this.name = name;
    }

    public Area(String name, ArrayList<Crop> crops){
        this.name = name;
        this.crops = crops;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public ArrayList<Crop> getCrops() {
        return crops;
    }

    public String toString(){
        return "Area: " + name + "; Crops: " + crops;
    }
}
