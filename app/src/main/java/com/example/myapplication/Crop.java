package com.example.myapplication;

public class Crop {
    private String cropName;
    private String waterRequired;

    public Crop(){

    }

    public Crop(String cropName, String waterRequired) {
        this.cropName = cropName;
        this.waterRequired = waterRequired;
    }

    public String getCropName() {
        return cropName;
    }

    public String getWaterRequired() {
        return waterRequired;
    }

    public String toString(){
        return "Crop name: " + cropName + ", Water required: " + waterRequired;
    }
}
