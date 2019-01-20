package org.fuckhkn.ieee_map;
//For Us, Completely Killing it at Hardhack is Key to Networking

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    private Button button;
    ArrayList<MarkerOptions> m = new ArrayList<>(); // list of flags

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        button = findViewById(R.id.button_class_switcher);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //code for button goes here
                if (button.getText().equals("Scooter")) {
                    button.setText("Bicycle");
                } else {
                    button.setText("Scooter");
                }
            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            checkLocationPermission();
        }

        checkNetworkConnection();
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place location markers

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        m.clear();
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        addMarker(m, location.getLatitude()-0.00013f, location.getLongitude()-0.000012f, BitmapDescriptorFactory.HUE_CYAN);
        addMarker(m, location.getLatitude()+0.000052f, location.getLongitude()-0.000038f, BitmapDescriptorFactory.HUE_ROSE);
        Random r = new Random(1214);
        for(int i=0;i<40;i++) {
            float n = (r.nextFloat()-0.5f)/42.0f;
            float p = (r.nextFloat()-0.6f)/72.0f;
            while(p>-0.004f && p<0.005f && n>0.001f && n<0.008f)
            {
                p = (r.nextFloat()-0.5f)/42.0f;
            }
            if (button.getText().equals("Scooter") && i%2==0) {
                addMarker(m, location.getLatitude() + n, location.getLongitude() + p, (i%2==0?BitmapDescriptorFactory.HUE_CYAN:BitmapDescriptorFactory.HUE_ROSE));
            } else if(button.getText().equals("Bicycle".toUpperCase()) && i%2==1) {
                addMarker(m, location.getLatitude() + n, location.getLongitude() + p, (i%2==0?BitmapDescriptorFactory.HUE_CYAN:BitmapDescriptorFactory.HUE_ROSE));
            }
            else {
                addMarker(m, location.getLatitude() + n, location.getLongitude() + p, (i%2==0?BitmapDescriptorFactory.HUE_CYAN:BitmapDescriptorFactory.HUE_ROSE));
            }

            addMarker(m, location.getLatitude() + 0.00174f, location.getLongitude() + 0.00117f, BitmapDescriptorFactory.HUE_YELLOW);
            addMarker(m, location.getLatitude() - 0.00361f, location.getLongitude() - 0.00132f, BitmapDescriptorFactory.HUE_YELLOW);
            addMarker(m, location.getLatitude() - 0.00196f, location.getLongitude() - 0.00571f, BitmapDescriptorFactory.HUE_YELLOW);
            addMarker(m, location.getLatitude() + 0.00684f, location.getLongitude() - 0.00615f, BitmapDescriptorFactory.HUE_YELLOW);
            addMarker(m, location.getLatitude() - 0.00173f, location.getLongitude() + 0.00134f, BitmapDescriptorFactory.HUE_YELLOW);
        }

        mMap.clear();
        for (int i=0;i<m.size();i++) {
            mMap.addMarker(m.get(i));
        }
        m.clear();

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        //send();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        try {
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_LOCATION: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        // permission was granted, yay! Do the
                        // location-related task you need to do.
                        if (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {

                            if (mGoogleApiClient == null) {
                                buildGoogleApiClient();
                            }
                            mMap.setMyLocationEnabled(true);
                        }
                    } else {

                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                        Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request

        } catch (Exception ex) {
        }
    }

    private void addMarker(ArrayList<MarkerOptions> markers, double x, double y, float color) {
        markers.add(new MarkerOptions().position(new LatLng(x, y)).icon(BitmapDescriptorFactory.defaultMarker(color)));
        //markers.add(new MarkerOptions().position(new LatLng(x,y)).icon(BitmapDescriptorFactory.fromFile("@mipmap/both.png")));
    }

    // check network connection
    public boolean checkNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = false;
        if (networkInfo != null && (isConnected = networkInfo.isConnected())) {
            // show "Connected" & type of network "WIFI or MOBILE"
            button.setText("Connected "+networkInfo.getTypeName());
            // change background color to red
            button.setBackgroundColor(0xFF7CCC26);


        } else {
            // show "Not Connected"
            button.setText("Not Connected");
            // change background color to green
            button.setBackgroundColor(0xFFFF0000);
        }

        return isConnected;
    }


    private String httpPost(String myUrl) throws IOException, JSONException {
        String result = "";

        URL url = new URL(myUrl);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        // 2. build JSON object
        JSONObject jsonObject = buildJsonObject();

        // 3. add JSON content to POST request body
        setPostRequestContent(conn, jsonObject);

        // 4. make POST request to the given URL
        conn.connect();

        // 5. return response message
        return conn.getResponseMessage()+"";

    }


    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    return httpPost(urls[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            button.setText(result);
        }
    }

    public void send() {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
        // perform HTTP POST request
        if(checkNetworkConnection()) {
            //new HTTPAsyncTask().execute("https://web.spin.pm/api/v1/auth_tokens");
            //new HTTPAsyncTask().execute("https://api.bird.co/user/login");
            new HTTPAsyncTask().execute("192.168.43.147");
        }
        else
            Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show();
    }

    private JSONObject buildJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        //jsonObject.accumulate("device", "{\"mobileType\":\"ios\",\"uid\":\"123e4567-e89b-12d3-a456-426655440000\"},\"grantType\":\"device\"}");
        //jsonObject.accumulate("email", "user@ucsd.edu");
//        jsonObject.accumulate("device","{\"Platform\":\"ios\",\"Device-id\":\"123e4567-e89b-12d3-a456-426655440000\"},\"Content-type\":\"application/json\"}");
        //jsonObject.accumulate("Platform","ios");
        //jsonObject.accumulate("Content-type","application/json");

        //jsonObject.accumulate("","");
return jsonObject;
    }

    private void setPostRequestContent(HttpURLConnection conn, JSONObject jsonObject) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        Log.i(MapsActivity.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }
}