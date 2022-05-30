package com.example.busstop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Math;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private RequestQueue queue;
    private final String url = "https://data.itsfactory.fi/journeys/api/1";
    private double userLat = 0;
    private double userLong = 0;
    private String closestStopId = null;
    private String closestStopName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        startGPS();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("userLat", this.userLat);
        outState.putDouble("userLong", this.userLong);
        outState.putString("closestStopId", this.closestStopId);
        outState.putString("closestStopName", this.closestStopName);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            this.userLat = savedInstanceState.getDouble("userLat");
            this.userLong = savedInstanceState.getDouble("userLong");
            this.closestStopId = savedInstanceState.getString("closestStopId");
            this.closestStopName = savedInstanceState.getString("closestStopName");
            updateFields();
        }
    }

    private void fetchData() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, this.url+"/stop-points",
                response -> {
                    try {
                        JSONArray json = new JSONObject(response).getJSONArray("body");
                        int len = new JSONObject(response).getJSONObject("data").getJSONObject("headers").getJSONObject("paging").getInt("pageSize");
                        getClosestStop(json, len);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        });
        queue.add(stringRequest);
    }

    private void startGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    0
            );
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        TextView textViewLat = (TextView) findViewById(R.id.textViewLat);
        TextView textViewLong = (TextView) findViewById(R.id.textViewLong);
        TextView textViewStop = (TextView) findViewById(R.id.textViewStopMain);
        textViewLat.setText(R.string.loading);
        textViewLong.setText(R.string.loading);
        textViewStop.setText(R.string.loading);
    }


    private void getClosestStop(JSONArray data, int length) {
        String closestStopId, closestStopName;
        Double closestStopDistance, lat, lon;
        try {
            closestStopId = data.getJSONObject(0).getString("shortName");
            closestStopName = data.getJSONObject(0).getString("name");
            String[] coordinates = data.getJSONObject(0).getString("location").split(",");
            lat = Double.parseDouble(coordinates[0]);
            lon = Double.parseDouble(coordinates[1]);
            closestStopDistance = Math.sqrt(Math.pow(lat - this.userLat, 2) + Math.pow(lon - this.userLong, 2));
            for (int i = 0; i < length; i++) {
                coordinates = data.getJSONObject(i).getString("location").split(",");
                lat = Double.parseDouble(coordinates[0]);
                lon = Double.parseDouble(coordinates[1]);
                Double distance = Math.sqrt(Math.pow(lat - this.userLat, 2) + Math.pow(lon - this.userLong, 2));
                if (distance < closestStopDistance) {
                    closestStopId = data.getJSONObject(i).getString("shortName");
                    closestStopName = data.getJSONObject(i).getString("name");
                    closestStopDistance = distance;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            closestStopId = null;
            closestStopName = null;
        }
        this.closestStopId = closestStopId;
        this.closestStopName = closestStopName;
    }

    public void openSchedule (View view) {
        Intent intent = new Intent(this, ScheduleActivity.class);
        intent.putExtra("STOP_ID", this.closestStopId);
        intent.putExtra("STOP_NAME", this.closestStopName);
        startActivity(intent);
    }

    private void updateFields () {
        TextView textViewLat = (TextView) findViewById(R.id.textViewLat);
        TextView textViewLong = (TextView) findViewById(R.id.textViewLong);
        TextView textViewStop = (TextView) findViewById(R.id.textViewStopMain);

        textViewLat.setText(String.format("%f", this.userLat));
        textViewLong.setText(String.format("%f", this.userLong));
        textViewStop.setText(this.closestStopName);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.userLat = location.getLatitude();
        this.userLong = location.getLongitude();

        fetchData();
        updateFields();
    }
}