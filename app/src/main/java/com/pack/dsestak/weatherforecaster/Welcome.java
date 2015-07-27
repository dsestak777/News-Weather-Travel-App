package com.pack.dsestak.weatherforecaster;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.widget.TextView;
import android.content.SharedPreferences;

public class Welcome extends Activity {

    Button weatherButton;
    TextView myAddress;

    private double latitude;
    private double longitude;
    private String locationAddress;
    private String oldAddress;
    LocationService locationService;
    SharedPreferenceManager sharedPrefMgr;

    Location gpsLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //get shared prefs
        sharedPrefMgr = new SharedPreferenceManager(this);
        oldAddress = sharedPrefMgr.getAddress();



        locationService = new LocationService(Welcome.this);

        setupViews();
        addButtonListeners();
        getLocation();
        getAddress();



    }

    //get our location using location service
    public void getLocation() {
                gpsLocation = locationService
                        .getLocation(LocationManager.GPS_PROVIDER);
                if (gpsLocation != null) {
                    latitude = gpsLocation.getLatitude();
                    longitude = gpsLocation.getLongitude();
                    String result = "Latitude: " + gpsLocation.getLatitude() +
                            " Longitude: " + gpsLocation.getLongitude();
                    myAddress.setText(result);
                //if GPS is off show alert
                } else {
                    showSettingsAlert();
                }
            }


    //use seperate thread to get GEOCODE location
    public void getAddress() {

                if (gpsLocation != null) {
                    latitude = gpsLocation.getLatitude();
                    longitude = gpsLocation.getLongitude();
                    GeoCoderHelper locationAddress = new GeoCoderHelper();
                    locationAddress.getAddressFromLocation(latitude, longitude,
                            getApplicationContext(), new GeoHandler());
                } else {
                    showSettingsAlert();
                }

            }

    //show alert dialog to remind user to turn on GPS
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                Welcome.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("You Must Enable Location Provider! Go to settings menu!");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        Welcome.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private class GeoHandler extends Handler {
        @Override
        public void handleMessage(Message message) {

            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            myAddress.setText(locationAddress);
            //add result to shared prefs
            sharedPrefMgr.setAddress(locationAddress);
        }
    }

    private void setupViews() {

        weatherButton = (Button) findViewById(R.id.weather_button);
        myAddress = (TextView) findViewById(R.id.address);

        //set old address unless we can get a new one
        if (oldAddress != null) {
            myAddress.setText(oldAddress+" - OLD DATA");
        //if no data is available from gps or shared prefs
        } else {
            myAddress.setText("No Data Available");
        }

    }

    private void addButtonListeners() {

        weatherButton.setOnClickListener (
            new View.OnClickListener() {

                @Override public void onClick(View v) {
                   Intent i = new Intent(Welcome.this, Weather.class);
                    i.putExtra("Lat", latitude);
                    i.putExtra("Long", longitude);
                    i.putExtra("Addr", locationAddress);
                    startActivity(i);
                    Welcome.this.finish();

                }
            }
        );


    }

//shutdown if back button is pressed
    @Override
    public void onBackPressed() {

        Welcome.this.finish();
        return;
    }

}
