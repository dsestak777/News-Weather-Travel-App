package com.pack.dsestak.weatherforecaster;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
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



public class Weather extends ListActivity {
	Button 		exitButton;

	private double longitude;
	private double latitude;
	private String address;
	private String oldAddress;
	private String oldWeather;
	private static String url;

	private JSONObject jArray = null;
	private JSONObject jArray2 = null;
	ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();

	SharedPreferenceManager sharedPrefMgr;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listplaceholder);

		//get shared prefs
		sharedPrefMgr = new SharedPreferenceManager(this);
		oldWeather = sharedPrefMgr.getWeather();
		oldAddress = sharedPrefMgr.getAddress();

		//get extras from intent
		Intent ii = getIntent();
		Bundle b = ii.getExtras();
		if (b!= null) {
			latitude = (Double) b.get("Lat");
			longitude = (Double) b.get("Long");
			address = (String) b.get("Addr");
		}

		setupViews();
		addButtonListeners();

		//truncate long & lat to use in url
		String l1 = String.format("%.2f", latitude);
		String l2 = String.format("%.2f", longitude);
		//set URL
		url = "http://www.myweather2.com/developer/forecast.ashx?uac=agDhyXZk1/&query=" + l1 + "," + l2 + "&temp_unit=f&output=json";

		//get weather data using JSON asynchronously
		new GetJSONFromUrl().execute();


	}



    private void setupViews()
    {
    	
        exitButton = 		(Button)findViewById(R.id.exit_button);


        
    }    
    
  private void addButtonListeners()
    {
        exitButton.setOnClickListener
    	(
    		new View.OnClickListener()
	    	{
				@Override public void onClick(View v) { 
				 startActivity(new Intent(Weather.this, Welcome.class));
			     Weather.this.finish(); }
			}
    	);
        
        
 
    }


	private class GetJSONFromUrl extends AsyncTask<String, String, JSONObject> {


		//InputStream is = null;
		//String result = "";
		String result2 = "";
		HttpURLConnection conn = null;

		private ProgressDialog dialog;

		//show dialog while retrieving data
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(Weather.this);
			dialog.setMessage("Getting Data ...");
			dialog.setIndeterminate(false);
			dialog.setCancelable(true);
			dialog.show();

		}

		//get data from service using JSON in background
		protected JSONObject doInBackground(String... args) {
			//response status
			int status=0;

			//http post
			try {

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

			/** This method was deprecated in SDK 22 .... Took me a while to get the new method (openConnection) to work
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(url);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			**/

			} catch (Exception e) {
				Log.e("log_tag", "Error in http connection " + e.toString());
			}



		//convert response to string
		try

		{
			//BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);

			//StringBuilder sb = new StringBuilder();
			//String line = null;
			//while ((line = reader.readLine()) != null) {
			//	sb.append(line + "\n");
			//}
			//reader.close();
			//is.close();
			//result = sb.toString();


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

				result2 = response.toString();

				//add result to shared prefs
				sharedPrefMgr.setWeather(result2);

				// print result
				//System.out.println(response.toString());
			//if no result use old data
			} else {
				System.out.println("GET request Error!");
				result2 = oldWeather;
				address = oldAddress+ " - OLD DATA";
				//System.out.println("old weather=" + oldWeather);
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

		//	jArray = new JSONObject(result);
		//	if (jArray == null) Log.d("jArray = ", "null");

			//create JSONObject array from result
			jArray2 = new JSONObject(result2);
			if (jArray2 == null) Log.d("jArray2 = ", "null");
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

			if (jArray2 != null) {
				try {
					//Get Local Weather
					//	JSONObject menuObject = jArray.getJSONObject("weather");
					JSONObject menuObject = jArray2.getJSONObject("weather");
					JSONArray currentweather = menuObject.getJSONArray("curren_weather");

					for (int i = 0; i < currentweather.length(); i++) {
						//create hashmap to store JSON data
						HashMap<String, String> map = new HashMap<String, String>();
						JSONObject e = currentweather.getJSONObject(0);

						JSONArray winddata = e.getJSONArray("wind");
						JSONObject wind = winddata.getJSONObject(0);

						//put data in map
						map.put("id", String.valueOf(i));
						map.put("location", "Location: " + address);
						map.put("temp", "Temperature:" + e.getString("temp"));
						map.put("weathertext", "Weather: " + e.getString("weather_text"));
						map.put("windspeed", "Wind Speed: " + wind.getString("speed") + " mph");
						mylist.add(map);

						//set data into listadapter
						ListAdapter adapter = new SimpleAdapter(Weather.this, mylist, R.layout.weather,
								new String[]{"location", "temp", "weathertext", "windspeed"},
								new int[]{R.id.item_title, R.id.item_subtitle, R.id.item_subtitle2, R.id.item_subtitle3});

						setListAdapter(adapter);

					}
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
				ListAdapter adapter = new SimpleAdapter(Weather.this, mylist, R.layout.weather,
						new String[]{"location", "temp", "weathertext", "windspeed"},
						new int[]{R.id.item_title, R.id.item_subtitle, R.id.item_subtitle2, R.id.item_subtitle3});

				setListAdapter(adapter);
			}


		}

	}

	private static String getResponseText(InputStream inStream) {

		return new Scanner(inStream).useDelimiter("\n").next();
	}

	}
    
