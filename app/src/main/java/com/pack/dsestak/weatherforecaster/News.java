package com.pack.dsestak.weatherforecaster;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class News extends ListActivity {
	Button exitButton;
	TextView resultsText;

	private static String url;

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

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listplaceholder);

		//get shared prefs
		sharedPrefMgr = new SharedPreferenceManager(this);

		//if network is not available show alert
		if (!isNetworkAvailable()) {

			showNetworkAlert();

		} else {

			//set URL to retrieve news from Yahoo
			url="https://query.yahooapis.com/v1/public/yql?q=select%20title%20from%20rss%20where%20url%3D%22http%3A%2F%2Frss.news.yahoo.com%2Frss%2Ftopstories%22&format=json&diagnostics=true&callback=";

			// get news data using JSON asynchronously
			new GetJSONFromYahoo().execute();
		}


		setupViews();
		resultsText.setText("WORLD NEWS:");
		addButtonListeners();


	}



	//show alert dialog to remind user to turn on Wi-fi or check connection
	public void showNetworkAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				News.this);
		alertDialog.setTitle("SETTINGS");
		alertDialog.setMessage("You Must Enable Wi-fi or have a Data Connection!");
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_WIFI_SETTINGS);
						News.this.startActivity(intent);
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
	private void setupViews() {

		exitButton = (Button) findViewById(R.id.exit_button);
		resultsText = (TextView) findViewById(R.id.results_title);


	}

	//add listeners for GUI buttons
	private void addButtonListeners() {
		exitButton.setOnClickListener
				(
						new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								startActivity(new Intent(News.this, Welcome.class));
								News.this.finish();
							}
						}
				);


	}

	//use asynctask to get data from Yahoo
	private class GetJSONFromYahoo extends AsyncTask<String, String, JSONObject> {


		String result = "";
		HttpURLConnection conn = null;

		private ProgressDialog dialog;


		//show dialog while retrieving data
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(News.this);
			dialog.setMessage("Getting Data ...");
			dialog.setIndeterminate(false);
			dialog.setCancelable(true);
			dialog.show();

		}

		//get data from service using JSON in background
		protected JSONObject doInBackground(String... args) {
			//response status
			int status = 0;

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
					System.out.println("new weather =" + response.toString());

					//if no result use old data
				} else {
					System.out.println("GET request Error!");

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
					//Get Headline News from JSON response
					JSONObject queryObject = jArray.getJSONObject("query");

					JSONObject results = queryObject.getJSONObject("results");
					JSONArray items = results.getJSONArray("item");

					for (int i = 0; i < items.length(); i++) {
						//create hashmap to store JSON data
						HashMap<String, String> map = new HashMap<String, String>();

						JSONObject title = items.getJSONObject(i);

						//put data in map
						map.put("news", title.getString("title"));

						mylist.add(map);

						//set data into listadapter
						ListAdapter adapter = new SpecialAdapter(News.this, mylist, R.layout.news,
								new String[]{"news"},
								new int[]{R.id.item_title});

						setListAdapter(adapter);

					}
				} catch (JSONException e) {
					Log.e("log_tag", "Error parsing data " + e.toString());
				}

				//if no data is available from the internet or shared prefs
			} else {


				HashMap<String, String> map = new HashMap<String, String>();
				map.put("id", String.valueOf(0));
				map.put("news", "News: not available");

				mylist.add(map);

				//set data into listadapter
				ListAdapter adapter = new SimpleAdapter(News.this, mylist, R.layout.news,
						new String[]{"news"},
						new int[]{R.id.item_title});

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


		Intent i = new Intent(News.this, Welcome.class);
		startActivity(i);
	}

}