package com.pack.dsestak.weatherforecaster;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferenceManager {

    Context mContext;

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    public SharedPreferenceManager(Context mContext) {

        settings = mContext.getSharedPreferences("my_shared_prefs", 0);
        editor = settings.edit();

    }

    //getters & setters for shared preferences
    public String getWeather() {return settings.getString("weather", null);}

    public void setWeather(String w) {
        editor.putString("weather", w);
        editor.commit();
    }

    public String getAddress() {return settings.getString("address", null);}

    public void setAddress(String add) {
        editor.putString("address", add);
        editor.commit();
    }

    public String getDestinationZipCode() {return settings.getString("destination_zip_code", null);}

    public void setDestinationZipCode(String zip) {
        editor.putString("destination_zip_code", zip);
        editor.commit();

    }

    public String getCurrentZipCode() {return settings.getString("current_zip_code", null);}

    public void setCurrentZipCode(String zip) {
        editor.putString("current_zip_code", zip);
        editor.commit();

    }
}