package com.pack.dsestak.weatherforecaster;

/**
 * Created by dsestak on 10/22/2015.
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;



public class Travel extends ListActivity {
    Button 		exitButton;

    private double longitude;
    private double latitude;
    private String address;
    //private String oldAddress;
    //private String oldWeather;
    private String destinationZip;
    private String currentZip;
    private static String url;
    private static final String YOUR_API_KEY = "AIzaSyCE5FLt79Fu_-WTbZfVmEi1USc6O-E151s";

    private JSONObject jArray = null;
    ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();

    SharedPreferenceManager sharedPrefMgr;

    //create boolean to check if network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listplaceholder);

        //get shared prefs
        sharedPrefMgr = new SharedPreferenceManager(this);
        destinationZip = sharedPrefMgr.getDestinationZipCode();


        //get extras from intent
        Intent ii = getIntent();
        Bundle b = ii.getExtras();

        if (b!= null) {
            latitude = (Double) b.get("Lat");
            longitude = (Double) b.get("Long");
            address = (String) b.get("Addr");
            currentZip = (String) b.get("zip");
        }

        setupViews();
        addButtonListeners();

        //if no zip code is stored show alert
        if (destinationZip == null) {

            showNoLocationAlert();

        //if network is not available show alert
        } else if(!isNetworkAvailable()) {

            showNetworkAlert();

        } else {

            //set URL to get data from Google
            url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+currentZip+"&destinations="+destinationZip+"&mode=driving&language=en-EN&units=imperial&key=AIzaSyCE5FLt79Fu_-WTbZfVmEi1USc6O-E151s";

            // get travel data using JSON asynchronously
            new GetJSONFromGoogleMaps().execute();

        }

    }


    //show alert dialog to remind user to enter a destination location
    public void showNoLocationAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                Travel.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("You Must Enter a Destination First!!");
        alertDialog.setPositiveButton("Enter Data",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Travel.this, EnterData.class);
                        Travel.this.startActivity(intent);
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
                Travel.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("You Must Enable Wi-fi or have a Data Connection!");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_WIFI_SETTINGS);
                        Travel.this.startActivity(intent);
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

    //setup GUI
    private void setupViews()
    {

        exitButton = 		(Button)findViewById(R.id.exit_button);



    }

    //add listeners for GUI buttons
    private void addButtonListeners()
    {
        exitButton.setOnClickListener
                (
                        new View.OnClickListener()
                        {
                            @Override public void onClick(View v) {
                                startActivity(new Intent(Travel.this, Welcome.class));
                                Travel.this.finish(); }
                        }
                );



    }

    private class GetJSONFromGoogleMaps extends AsyncTask<String, String, JSONObject> {


        String result = "";
        HttpURLConnection conn = null;

        private ProgressDialog dialog;

        //show dialog while retrieving data
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Travel.this);
            dialog.setMessage("Getting Data ...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);
            dialog.show();

        }

        //get data from service using JSON in background
        protected JSONObject doInBackground(String... args) {
            //response status
            int status=0;

            //http GET request
            try {
                //create a connection
                URL u = new URL(url);
                conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setUseCaches(false);
                conn.setAllowUserInteraction(false);
                conn.connect();
                status = conn.getResponseCode();
                Log.d("Status", "Status=" + status);



            } catch (Exception e) {
                Log.e("log_tag", "Error in http connection " + e.toString());
            }

            //convert response to string
            try

            {

                if (status == HttpURLConnection.HTTP_OK) { // success
                    //create new buffered reader
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    String inputLine;

                    //create stringbuilder for response
                    StringBuilder response = new StringBuilder();

                    //add data to StringBuilder
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    result = response.toString();

                    //add result to shared prefs
                    sharedPrefMgr.setWeather(result);

                    // print result
                    System.out.println("new travel =" + response.toString());

                    //if no result use old data
                } else {
                    System.out.println("GET request Error!");

                }

            }

            catch(
                    Exception e
                    )

            {
                Log.e("log_tag", "Error converting result " + e.toString());
            }

            try

            {
                //create JSONObject array from result
                jArray = new JSONObject(result);
                if (jArray == null) Log.d("jArray = ", "null");
            }

            catch(
                    JSONException e
                    )

            {
                Log.e("log_tag", "Error parsing data " + e.toString());
            }

            return jArray;

        }
        protected void onPostExecute(JSONObject jArray) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (jArray != null) {
                try {
                    //Get Travel info from JSON response
                    JSONArray originObject = jArray.getJSONArray("origin_addresses");
                    JSONArray destinationObject = jArray.getJSONArray("destination_addresses");
                    JSONArray rowArray = jArray.getJSONArray("rows");


                    String rows = rowArray.toString();
                    System.out.println("rows=" + rows);


                    JSONObject elementObject = rowArray.getJSONObject(0);
                    String elemObj = elementObject.toString();

                    JSONArray elementArray = elementObject.getJSONArray("elements");

                    String elemArr = elementArray.toString();

                    System.out.println("element Obj = " + elemObj);
                    System.out.println("element Array = " + elemArr);

                    JSONObject elementZero = elementArray.getJSONObject(0);

                    String status = elementZero.getString("status");
                    String duration = elementZero.getString("duration");
                    String origin = originObject.toString();
                    String destination = destinationObject.toString();

                    String[] dur = duration.split(",");
                    String[] time = dur[1].split(":");
                    String travelTime = time[1];
                    travelTime = travelTime.substring(0, travelTime.length()-1);


                    String distance = elementZero.getString("distance");
                    String[] dist = distance.split(",");
                    String[] d = dist[1].split(":");
                    String travelDistance = d[1];
                    travelDistance = travelDistance.substring(0, travelDistance.length()-1);


                    System.out.println("element duration = " + elementZero);
                    System.out.println("status = " + status);
                    System.out.println("duration = " + duration);
                    System.out.println("travel time = " + travelTime);
                    System.out.println("travel distance = " + travelDistance);



                        //create hashmap to store JSON data
                        HashMap<String, String> map = new HashMap<String, String>();


                        //put data in map
                        map.put("origin", "Origin:" + origin);
                        map.put("destination", "Destination:" + destination);
                        map.put("distance", "Travel Distance:" + travelDistance);
                        map.put("time", "Travel Time:" + travelTime);


                        mylist.add(map);

                        //set data into listadapter
                        ListAdapter adapter = new SimpleAdapter(Travel.this, mylist, R.layout.weather,
                                new String[]{"origin","destination","distance", "time"},
                                new int[]{R.id.item_title, R.id.item_subtitle, R.id.item_subtitle2, R.id.item_subtitle3});

                        setListAdapter(adapter);

                  //  }
                } catch (JSONException e) {
                    Log.e("log_tag", "Error parsing data " + e.toString());
                }

                //if no data is available from the internet or shared prefs
            } else {


                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", String.valueOf(0));
                map.put("location", "Location: not available" );
                map.put("temp", "Temperature: not available" );
                map.put("weathertext", "Weather: not available" );
                map.put("windspeed", "Wind Speed: not available");
                mylist.add(map);

                //set data into listadapter
                ListAdapter adapter = new SimpleAdapter(Travel.this, mylist, R.layout.weather,
                        new String[]{"location", "temp", "weathertext", "windspeed"},
                        new int[]{R.id.item_title, R.id.item_subtitle, R.id.item_subtitle2, R.id.item_subtitle3});

                setListAdapter(adapter);
            }


        }

    }



    private static String getResponseText(InputStream inStream) {

        return new Scanner(inStream).useDelimiter("\n").next();
    }

    //return to main menu if back button is pressed
    @Override
    public void onBackPressed() {


        Intent i = new Intent(Travel.this, Welcome.class);
        startActivity(i);
    }

}

