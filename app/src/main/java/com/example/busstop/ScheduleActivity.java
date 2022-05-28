package com.example.busstop;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ScheduleActivity extends AppCompatActivity {
    private RequestQueue queue;
    private final String url = "https://data.itsfactory.fi/journeys/api/1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        queue = Volley.newRequestQueue(this);
        String stopName = getIntent().getStringExtra("STOP_NAME");
        String stopId = getIntent().getStringExtra("STOP_ID");

        TextView textViewStopName = findViewById(R.id.textViewStopName);
        textViewStopName.setText(stopName);
        fetchBusses(stopId);
    }

    private void fetchBusses(String stopId) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, this.url+"/stop-monitoring?stops="+stopId,
                response -> {
                    try {
                        JSONArray json = new JSONObject(response).getJSONObject("body").getJSONArray(stopId);
                        int len = json.length();
                        updateText(json, len);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        });
        queue.add(stringRequest);
    }

    private void updateText(JSONArray json, int len) {
        String result = "";
        try {
            for (int i = 0; i < len; i++) {
                String bus = json.getJSONObject(i).getString("lineRef");
                String time = json.getJSONObject(i).getJSONObject("call").getString("expectedArrivalTime").substring(11, 16);
                result += String.format("Bus %s - %s\n", bus, time);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (result != "") {
            TextView textViewBusses = findViewById(R.id.textViewBusses);
            textViewBusses.setText(result);
        }
    }
}