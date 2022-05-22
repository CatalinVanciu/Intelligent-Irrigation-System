package com.example.myapplication;

import java.util.ArrayList;

public class RetrieveDataFromFirebaseHelper {
    private static RetrieveDataFromFirebaseHelper instance;

    public ArrayList<Area> areas = new ArrayList<>();

    private RetrieveDataFromFirebaseHelper(){

    }

    public static RetrieveDataFromFirebaseHelper getInstance(){
        if(instance == null){
            instance = new RetrieveDataFromFirebaseHelper();
        }
        return instance;
    }

}
