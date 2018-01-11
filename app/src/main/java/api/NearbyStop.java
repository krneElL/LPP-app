package api;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


import db.DatabaseHelper;
import tables.Stop;


/**
 * Created by asrsa on 10.1.2018.
 */

public class NearbyStop {

    final private String address;
    final private int radius;
    private DatabaseHelper db;

    private Double posLat;
    private Double posLon;


    public NearbyStop(Context context, String address, int radius) {
        this.address = address;
        this.radius = radius;
        this.db = new DatabaseHelper(context);
    }

    /**
     * Returns list of valid stops that are nearby the address in radius
     * @return ArrayList<Stop> list of stops that are nearby
     * */
    public ArrayList<Stop> getNearbyStops() {
        //getAddressLocation();
        JSONArray stopsData = getStops();

        //check if each stop is contained in the database
        ArrayList<Stop> stops = new ArrayList<>();
        if(stopsData != null) {
            try {
                for (int i = 0; i < stopsData.length(); i++) {
                    JSONObject stop = stopsData.getJSONObject(i);
                    int stopId = stop.getInt("int_id");

                    if(db.containsStop(stopId)) {
                        Double lat = stop.getDouble("latitude");
                        Double lon = stop.getDouble("longitude");
                        String name = stop.getString("name");

                        stops.add(new Stop(stopId, lat, lon, name));
                    }
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }
        }

        return stops;
    }

    /**
     * Gets latitude and longitude of a given address, always takes the first occurrence
     * */
    public void getAddressLocation() {
        final String API_URL_GMAPS = "https://maps.google.si/maps/api/geocode/json";
        final String API_KEY = "AIzaSyDm5cNT-45-aS_n6Vk8CwhW2BlQLlmyYC8";

        NearbyApiCall api = new NearbyApiCall(API_URL_GMAPS);
        try {
            String response =  api.execute(new HashMap<String, String>()
                                    {{
                                        put("address", address);
                                        put("key", API_KEY);
                                    }}).get();

            JSONObject json = new JSONObject(response);
            if(json.getString("status").equals("OK")) {
                JSONObject data = json.getJSONArray("results").getJSONObject(0);
                JSONObject geometry = data.getJSONObject("geometry");

                posLat = geometry.getJSONObject("location").getDouble("lat");
                posLon = geometry.getJSONObject("location").getDouble("lng");
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }

    }

    /**
     * Returns a JSONArray with stops from API response
     * @return JSONArray with response from the API call
     * */
    private JSONArray getStops() {
        final String API_URL_LPP = "http://data.lpp.si/stations/stationsInRange";
        NearbyApiCall api = new NearbyApiCall(API_URL_LPP);
        try {
            String response = api.execute(new HashMap<String, String>()
                                        {{
                                            put("lat", String.valueOf(posLat));
                                            put("lon", String.valueOf(posLon));
                                            put("radius", String.valueOf(radius));
                                        }}).get();

            JSONObject json = new JSONObject(response);

            if(json.getString("success").equals("true")) {
                return json.getJSONArray("data");
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }
        return null;
    }

    public Double getPosLat() {
        return posLat;
    }

    public Double getPosLon() {
        return posLon;
    }
}
