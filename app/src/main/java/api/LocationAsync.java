package api;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lppapp.ioi.lpp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import db.DatabaseHelper;
import tables.BusLocation;
import tables.Stop;

/**
 * Created by Citrus on 19.12.2017.
 */

public class LocationAsync extends AsyncTask<String, ArrayList<MarkerOptions>, ArrayList<BusLocation>> {

    private GoogleMap map;
    private static Timer backgroundTimer;
    private DatabaseHelper db;

    private final int DELAY_MS = 100;
    private final int PERIOD_MS = 5000;
    private final int ADD_SECONDS = 60;

    private ArrayList<Marker> prevMarkers;



    public LocationAsync(Context context, GoogleMap map) {
        this.map = map;

        this.db = new DatabaseHelper(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //backgroundTimer = new Timer();
        this.prevMarkers = new ArrayList<>();
    }

    @Override
    protected ArrayList<BusLocation> doInBackground(final String... param) {
        if(this.db != null) {
            HashMap<Integer, BusLocation> currBuses = new HashMap<>();

            while(!isCancelled()) {
                publishProgress(null);

                try {
                    ArrayList<MarkerOptions> markerList = new ArrayList<>();
                    long startTime = System.currentTimeMillis();
                    for(String route : param) {
                        HashMap<Integer, BusLocation> busLocations = getCurrentLocations(route);

                        if(currBuses.isEmpty()) {
                            currBuses = busLocations;
                        }
                        else {
                            for(Map.Entry<Integer, BusLocation> entry : busLocations.entrySet()) {
                                int key = entry.getKey();
                                BusLocation bus = entry.getValue();

                                //update bus location if it exists in the currBuses
                                if(currBuses.containsKey(key)) {
                                    currBuses.remove(key);
                                    currBuses.put(key, bus);
                                }

                                //insert new bus to currBuses
                                if(!currBuses.containsKey(key)) {
                                    currBuses.put(key, bus);
                                }
                            }
                        }
                        markerList.clear();

                        for(Map.Entry<Integer, BusLocation> entry : currBuses.entrySet()) {
                            int key = entry.getKey();
                            BusLocation bus = entry.getValue();

                            MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(bus.lat, bus.lon))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.trolamarker));

                            if(!busLocations.containsKey(key)) {
                                markerOpt.alpha(0.5f);
                            }

                            //questionable data
                            Double eta = calculateNextStopETA(bus.lat, bus.lon, bus.speed, bus.station_int_id);
                            markerOpt.title("Prihod: " + Double.toString(eta));

                            markerList.add(markerOpt);
                        }
                    }
                    long endTime = System.currentTimeMillis();

                    System.out.println("That took " + (endTime - startTime) + " milliseconds");
                    publishProgress(markerList);
                    Thread.sleep(this.PERIOD_MS);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }

        }
        return null;
    }



    @Override
    protected void onPostExecute(ArrayList<BusLocation> locations) {
        Log.e("INFO", "Call inside onPostExecute.");
    }

    @Override
    protected void onProgressUpdate(ArrayList<MarkerOptions>[] values) {
        super.onProgressUpdate(values);

        if(values != null) {
            if (values[0] == null) {
                Log.e("INFO", "Inside onProgress == null.");
            } else if (values[0] != null) {
                if(!this.prevMarkers.isEmpty()){

                    for(Marker marker : this.prevMarkers) {
                        marker.remove();
                    }
                    this.prevMarkers.clear();
                }

                Log.e("INFO", "Call inside progress.");

                for(MarkerOptions marker : values[0]) {
                    this.prevMarkers.add(this.map.addMarker(marker));
                }
            }
        }
    }

    private HashMap<Integer, BusLocation> getCurrentLocations(String route_int_id) {
        Calendar currCal = Calendar.getInstance();
        Date currDate = currCal.getTime();
        currCal.add(Calendar.SECOND, this.ADD_SECONDS);
        Date nextDate = currCal.getTime();

        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        String currTime = "11 " + df.format(currDate);
        String nextTime = "11 " + df.format(nextDate);


        //check currentTime as if it was 11th December 2017
        return db.getBusLocationByRouteId(route_int_id, currTime, nextTime);
    }

    /**
     * Calculates how many seconds bus needs to the next stop that is stored in the database
     * @param busLat latitude of live bus
     * @param busLon longitutde of live bus
     * @param speed speed in km/h of live bus
     * @param nextStopId next stop of live bus, data is questionable
     * */
    private Double calculateNextStopETA(Double busLat, Double busLon, int speed, int nextStopId) {
        if(nextStopId != 0) {
            Stop nextStop = db.getStopById(nextStopId);

            float[] distanceResult = new float[1];
            Location.distanceBetween(busLat, busLon, nextStop.latitude, nextStop.longitude, distanceResult);
            Double distanceKM = distanceResult[0] / 1000d;

            Double eta = (distanceKM / speed) * 3600;

            return eta;
        }
        return -1d;
    }
}
