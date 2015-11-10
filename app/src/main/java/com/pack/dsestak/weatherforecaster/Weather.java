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
	Button exitButton;
	TextView resultsText;

	private double longitude;
	private double latitude;
	private String address;
	private String oldAddress;
	private String oldWeather;
	private String destinationZip;
	private static String url;

	private JSONObject jArray = null;
	ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();

	SharedPreferenceManager sharedPrefMgr;

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listplaceholder);

		//get shared prefs
		sharedPrefMgr = new SharedPreferenceManager(this);
		//oldWeather = sharedPrefMgr.getWeather();
		//oldAddress = sharedPrefMgr.getAddress();
		destinationZip = sharedPrefMgr.getDestinationZipCode();

		if (destinationZip == null) {

			showNoLocationAlert();

		} else if (!isNetworkAvailable()) {

			showNetworkAlert();

		} else {

			//	url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%2222204%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

			url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22" + destinationZip + "%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";


			// get weather data using JSON asynchronously
			new GetJSONFromYahoo().execute();
		}
		//get extras from intent
		//	Intent ii = getIntent();
		//	Bundle b = ii.getExtras();
		//	if (b!= null) {
		//		latitude = (Double) b.get("Lat");
		//		longitude = (Double) b.get("Long");
		//		address = (String) b.get("Addr");
		//	}

		setupViews();
		resultsText.setText("RESULTS FOR : " + destinationZip);
		addButtonListeners();

		//truncate long & lat to use in url
		//String l1 = String.format("%.2f", latitude);
		//String l2 = String.format("%.2f", longitude);
		//set URL

		//url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22nome%2C%20ak%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
		//url = "http://weather.yahooapis.com/forecastrss?w=2295425&u=c&#8221";


	}

	//show alert dialog to remind user to enter a destination location
	public void showNoLocationAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				Weather.this);
		alertDialog.setTitle("SETTINGS");
		alertDialog.setMessage("You Must Enter a Destination First!!");
		alertDialog.setPositiveButton("Enter Data",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Weather.this, EnterData.class);
						Weather.this.startActivity(intent);
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
				Weather.this);
		alertDialog.setTitle("SETTINGS");
		alertDialog.setMessage("You Must Enable Wi-fi or have a Data Connection!");
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_WIFI_SETTINGS);
						Weather.this.startActivity(intent);
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

	private void setupViews() {

		exitButton = (Button) findViewById(R.id.exit_button);
		resultsText = (TextView) findViewById(R.id.results_title);


	}

	private void addButtonListeners() {
		exitButton.setOnClickListener
				(
						new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								startActivity(new Intent(Weather.this, Welcome.class));
								Weather.this.finish();
							}
						}
				);


	}

	private class GetJSONFromYahoo extends AsyncTask<String, String, JSONObject> {


		String result = "";
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
			int status = 0;

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
					System.out.println("new weather =" + response.toString());

					//if no result use old data
				} else {
					System.out.println("GET request Error!");
					result = oldWeather;
					address = oldAddress + " - OLD DATA";
					System.out.println("old weather=" + oldWeather);
				}


			} catch (
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
			} catch (
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
					//Get Local Weather
					//	JSONObject menuObject = jArray.getJSONObject("weather");
					JSONObject queryObject = jArray.getJSONObject("query");
					//JSONArray currentweather = menuObject.getJSONArray("results");

					JSONObject results = queryObject.getJSONObject("results");
					JSONObject channel = results.getJSONObject("channel");
					JSONObject item = channel.getJSONObject("item");
					JSONArray forecast = item.getJSONArray("forecast");


					for (int i = 0; i < forecast.length(); i++) {
						//create hashmap to store JSON data
						HashMap<String, String> map = new HashMap<String, String>();

						//JSONObject e = currentweather.getJSONObject(0);

						//	JSONArray winddata = e.getJSONArray("wind");
						//	JSONObject wind = winddata.getJSONObject(0);

						JSONObject currentForecast = forecast.getJSONObject(i);

						//put data in map
						map.put("day", currentForecast.getString("day"));
						map.put("high", currentForecast.getString("high") + " high");
						map.put("low", currentForecast.getString("low") + " low");
						map.put("text", currentForecast.getString("text"));
						//	map.put("id", String.valueOf(i));
						//	map.put("location", "Location: " + address);
						//	map.put("temp", "Temperature:" + e.getString("temp"));
						//	map.put("weathertext", "Weather: " + e.getString("weather_text"));
						//		map.put("windspeed", "Wind Speed: " + wind.getString("speed") + " mph");
						mylist.add(map);

						//set data into listadapter
						ListAdapter adapter = new SpecialAdapter(Weather.this, mylist, R.layout.weather,
								new String[]{"day", "high", "low", "text"},
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
				map.put("location", "Location: not available");
				map.put("temp", "Temperature: not available");
				map.put("weathertext", "Weather: not available");
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


	private class GetJSONFromUrl extends AsyncTask<String, String, JSONObject> {


		String result = "";
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
			int status = 0;

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
					System.out.println(response.toString());

					//if no result use old data
				} else {
					System.out.println("GET request Error!");
					result = oldWeather;
					address = oldAddress + " - OLD DATA";
					System.out.println("old weather=" + oldWeather);
				}


			} catch (
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
			} catch (
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
					//Get Local Weather
					//	JSONObject menuObject = jArray.getJSONObject("weather");
					JSONObject menuObject = jArray.getJSONObject("weather");
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
				map.put("location", "Location: not available");
				map.put("temp", "Temperature: not available");
				map.put("weathertext", "Weather: not available");
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


	//return to main menu if back button is pressed
	@Override
	public void onBackPressed() {


		Intent i = new Intent(Weather.this, Welcome.class);
		startActivity(i);
	}

}