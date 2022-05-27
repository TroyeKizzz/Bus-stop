package com.example.busstop;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Math;

public class MainActivity extends AppCompatActivity {
    private RequestQueue queue;
    String url = "https://data.itsfactory.fi/journeys/api/1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
    }

    public void fetchData(View view) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, this.url+"/stop-points",
                response -> {
                    Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
                    try {
                        JSONArray json = new JSONObject(response).getJSONArray("body");
                        int len = new JSONObject(response).getJSONObject("data").getJSONObject("headers").getJSONObject("paging").getInt("pageSize");
                        getClosestStopId(json, len);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        });
        queue.add(stringRequest);
    }

    private String getClosestStopId(JSONArray data, int length) {
        String closestStopId, closestStopName;
        Double closestStopDistance, lat, lon;
        try {
            closestStopId = data.getJSONObject(0).getString("shortName");
            closestStopName = data.getJSONObject(0).getString("name");
            String[] coordinates = data.getJSONObject(0).getString("location").split(",");
            lat = Double.parseDouble(coordinates[0]);
            lon = Double.parseDouble(coordinates[1]);
            closestStopDistance = Math.sqrt(Math.pow(lat - 61.50298, 2) + Math.pow(lon - 23.80904, 2));
            for (int i = 0; i < length; i++) {
                coordinates = data.getJSONObject(i).getString("location").split(",");
                lat = Double.parseDouble(coordinates[0]);
                lon = Double.parseDouble(coordinates[1]);
                Double distance = Math.sqrt(Math.pow(lat - 61.50298, 2) + Math.pow(lon - 23.80904, 2));
                if (distance < closestStopDistance) {
                    closestStopId = data.getJSONObject(i).getString("shortName");
                    closestStopName = data.getJSONObject(i).getString("name");
                    closestStopDistance = distance;
                }
            }
            Toast.makeText(this, closestStopName + ": " + closestStopDistance, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
            closestStopId = "0000";
        }
        return closestStopId;
    }
}