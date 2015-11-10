package com.pack.dsestak.weatherforecaster;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Pattern;

/**
 * Created by dsestak on 11/3/2015.
 */
public class EnterData extends Activity {

    EditText myAddress;
    EditText myCity;
    EditText myState;
    EditText myZipCode;
    Button enterButton, exitButton;
    SharedPreferenceManager sharedPrefMgr;

    @Override
    protected void onCreate (Bundle icicle) {
            super.onCreate(icicle);
            setContentView(R.layout.enter_data);

            //get shared prefs
            sharedPrefMgr = new SharedPreferenceManager(this);

            setupViews();
            addButtonListeners();
    }


    private  void setupViews () {

        myAddress = (EditText) findViewById(R.id.address);
        myCity = (EditText) findViewById(R.id.city);
        myState = (EditText) findViewById(R.id.state);
        myZipCode = (EditText) findViewById(R.id.zip_code);
        enterButton = (Button)findViewById(R.id.enter_button);
        exitButton = (Button)findViewById(R.id.exit_button);


    }

    //show alert dialog to remind user to enter a properly formatted zip code
    public void showZipFormatAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                EnterData.this);
        alertDialog.setTitle("ZIP CODE");
        alertDialog.setMessage("You Must Enter a Properly Formatted Zip Code!!");

        alertDialog.setNegativeButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private void addButtonListeners () {

        enterButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        {
                            String zipCheck = myZipCode.getText().toString();

                            //check if zip is all numbers
                            if (Pattern.matches("[a-zA-Z]+",zipCheck)) {showZipFormatAlert();}

                            //check zip code length
                            else if (zipCheck.length()>5 || zipCheck.length() < 5) {showZipFormatAlert();}


                            else {
                                Intent intent = new Intent(EnterData.this, Welcome.class);
                                String zip = myZipCode.getText().toString();
                                sharedPrefMgr.setDestinationZipCode(zip);
                                startActivity(intent);
                                EnterData.this.finish();
                                ;
                            }
                        }
                    }
                }
        );

        exitButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        {
                            Intent intent = new Intent(EnterData.this, Welcome.class);
                            startActivity(intent);
                            EnterData.this.finish();;
                        }
                    }
                }
        );
    }
}
