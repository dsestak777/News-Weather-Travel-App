package com.pack.dsestak.weatherforecaster;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by dsestak on 7/25/2015.
 */
public class GeoCoderHelper {

    private static final String TAG = "LocationAddress";

    public static void getAddressFromLocation(final double latitude, final double longitude,
                                              final Context context, final Handler handler) {

        //start thread to use GEOCODER
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                String zipResult = null;

                try {
                    //store location information in List
                    List<Address> addressList = geocoder.getFromLocation(
                            latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        //use stringbuilder to create formatted response string
                        Address address = addressList.get(0);
                        StringBuilder sb1 = new StringBuilder();
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            sb1.append(address.getAddressLine(i)).append("\n");
                        }
                      //  sb1.append(address.getLocality()).append("\n");
                      //  sb1.append(address.getPostalCode()).append("\n");
                      //  sb1.append(address.getCountryName());
                        result = sb1.toString();
                        //put zip code info into separate string
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(address.getPostalCode());
                        zipResult = sb2.toString();                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable connect to Geocoder", e);
                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    //set results if not null
                    if (result != null) {
                        message.what = 1;
                        //create bundle & add data
                        Bundle bundle = new Bundle();
                        result = "Latitude: " + latitude + " Longitude: " + longitude +
                                "\nAddress:\n" + result;
                        bundle.putString("address", result);
                        bundle.putString("zip_code", zipResult);
                        message.setData(bundle);
                    //if no results
                    } else {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        result = "Latitude: " + latitude + " Longitude: " + longitude +
                                "\n Unable to get address for this lat-long.";
                        zipResult = "unable to get zip code for this lat-long";
                        bundle.putString("address", result);
                        bundle.putString("zip_code", zipResult);

                        message.setData(bundle);
                    }
                    message.sendToTarget();
                }
            }
        };
        thread.start();
    }
}

