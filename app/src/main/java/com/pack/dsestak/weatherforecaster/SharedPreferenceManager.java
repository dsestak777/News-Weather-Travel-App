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
}