package com.pack.dsestak.weatherforecaster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
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

public class Welcome extends Activity  {

    Button weatherButton, travelButton, destinationButton, newsButton;
    TextView myAddress;

    private double latitude;
    private double longitude;
    private String locationAddress;
    private String currentZip;
    private String oldAddress;
    LocationService locationService;
    SharedPreferenceManager sharedPrefMgr;

    protected LocationManager locationManager;
    Location gpsLocation;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    private Context mContext = Welcome.this;

    //create boolean to check if network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //get shared prefs
        sharedPrefMgr = new SharedPreferenceManager(this);
        oldAddress = sharedPrefMgr.getAddress();

        //get an instance of the location service
        locationService = new LocationService(Welcome.this);
        locationManager = (LocationManager) mContext
                .getSystemService(LOCATION_SERVICE);

        // getting GPS status
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        setupViews();
        addButtonListeners();
        getAddress();

    }

    //use separate thread to get GEOCODE location
    public void getAddress() {

        gpsLocation = locationService
                .getLocation(LocationManager.GPS_PROVIDER);

        //check if both are enabled prior to getting location & Geocode
        if (isGPSEnabled && isNetworkAvailable()) {
           if (gpsLocation != null) {

               latitude = gpsLocation.getLatitude();
               longitude = gpsLocation.getLongitude();

               String result = "Latitude: " + gpsLocation.getLatitude() +
                       " Longitude: " + gpsLocation.getLongitude();

               System.out.println("gps results = " + result);


               GeoCoderHelper locationAddress = new GeoCoderHelper();
               locationAddress.getAddressFromLocation(latitude, longitude,
                       getApplicationContext(), new GeoHandler());
           }
            //if GPS is off show alert
        } else if (!isGPSEnabled) {

               showSettingsAlert();

           //if network is not available show alert
        } else if (isNetworkAvailable()) {

               showNetworkAlert();

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

    //show alert dialog to remind user to turn on Wi-fi or check connection
    public void showNetworkAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                Welcome.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("You Must Enable Wi-fi or have a Data Connection!");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_WIFI_SETTINGS);
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
                    System.out.println("bundle =" + bundle);
                    locationAddress = bundle.getString("address");
                    currentZip = bundle.getString("zip_code");
                    break;
                default:
                    locationAddress = null;
            }
            myAddress.setText(locationAddress);
            //add result to shared prefs
            sharedPrefMgr.setAddress(locationAddress);
        }
    }

    //setup GUI
    private void setupViews() {
        travelButton = (Button) findViewById(R.id.travel_button);
        weatherButton = (Button) findViewById(R.id.weather_button);
        destinationButton = (Button) findViewById(R.id.enter_info_button);
        newsButton = (Button) findViewById(R.id.news_button);
        myAddress = (TextView) findViewById(R.id.address);


        //set old address unless we can get a new one
        if (oldAddress != null) {
            myAddress.setText(oldAddress+" - OLD DATA");
        //if no data is available from gps or shared prefs
        } else {
            myAddress.setText("No Data Available");
        }

    }

    //add listeners for GUI buttons
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


        newsButton.setOnClickListener (
                new View.OnClickListener() {

                    @Override public void onClick(View v) {
                        Intent i = new Intent(Welcome.this, News.class);



                        startActivity(i);
                        Welcome.this.finish();

                    }
                }
        );

        travelButton.setOnClickListener (
                new View.OnClickListener() {

                    @Override public void onClick(View v) {
                        Intent i = new Intent(Welcome.this, Travel.class);
                        i.putExtra("Lat", latitude);
                        i.putExtra("Long", longitude);
                        i.putExtra("Addr", locationAddress);
                        i.putExtra("zip", currentZip);

                        startActivity(i);
                        Welcome.this.finish();

                    }
                }
        );

        destinationButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Welcome.this, EnterData.class);

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
