package com.wadektech.mtihani.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.wadektech.mtihani.persistence.MtihaniRevise;

public class Constants {
    public static final String FIREBASE_URL = "";
    public static final String FIREBASE_LOCATION_USERS = "users";
    public static final String FIREBASE_URL_USERS = FIREBASE_URL + "/" + FIREBASE_LOCATION_USERS;
    public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";
    public static final String myPreferences = "myPreferences";
    public static final String userId = "userId";
    public static final String imageURL = "imageURL";
    public static final String userName = "userName";
    public static final String email = "email";
    public static final String status = "statusUpdate";
    public static final String prefKey = "key";

    public static String getUserId(){
        SharedPreferences pfs = MtihaniRevise
                .Companion
                .getApp()
                .getApplicationContext()
                .getSharedPreferences(Constants.myPreferences,Context.MODE_PRIVATE);
        return pfs.getString(Constants.userId,"");
    }

    public static String getEmail(){
        SharedPreferences pfs = MtihaniRevise
                .Companion
                .getApp()
                .getApplicationContext()
                .getSharedPreferences(Constants.myPreferences,Context.MODE_PRIVATE);
        return pfs.getString(Constants.email,"");
    }

    public static String getUserName(){
        SharedPreferences pfs = MtihaniRevise
                .Companion
                .getApp()
                .getApplicationContext()
                .getSharedPreferences(Constants.myPreferences,Context.MODE_PRIVATE);
        return pfs.getString(Constants.userName,"");
    }

    public static String getImageURL() {
        SharedPreferences pfs = MtihaniRevise
                .Companion
                .getApp()
                .getApplicationContext()
                .getSharedPreferences(Constants.myPreferences,Context.MODE_PRIVATE);
        return pfs.getString(Constants.imageURL,"");
    }

    public static String getStatus() {
        SharedPreferences pfs = MtihaniRevise
                .Companion
                .getApp()
                .getApplicationContext()
                .getSharedPreferences(Constants.myPreferences,Context.MODE_PRIVATE);
        return pfs.getString(Constants.status,"");
    }

    public static String getSharedPreferenceKey() {
        SharedPreferences pfs = MtihaniRevise
                .Companion
                .getApp()
                .getApplicationContext()
                .getSharedPreferences(Constants.myPreferences,Context.MODE_PRIVATE);
        return pfs.getString(Constants.prefKey,"");
    }
}
